/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */
package org.gluu.oxtrust.service.scim2;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.inject.Named;

import org.gluu.oxtrust.ldap.service.IGroupService;
import org.gluu.oxtrust.model.GluuGroup;
import org.gluu.oxtrust.model.scim2.Group;
import org.gluu.oxtrust.service.external.ExternalScimService;
import org.gluu.oxtrust.util.CopyUtils2;
import org.gluu.oxtrust.util.ServiceUtil;
import org.gluu.site.ldap.exception.DuplicateEntryException;
import org.gluu.site.ldap.persistence.exception.EntryPersistenceException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;

/**
 * Centralizes calls by the GroupWebService and BulkWebService service classes
 *
 * @author Val Pecaoco
 */
@Stateless
@Named
public class Scim2GroupService implements Serializable {

    @Inject
    private Logger log;

    @Inject
    private IGroupService groupService;

    @Inject
    private ExternalScimService externalScimService;

    @Inject
    private CopyUtils2 copyUtils2;
    
    @Inject
    private ServiceUtil serviceUtil;

    public Group createGroup(Group group) throws Exception {
        log.debug(" copying gluuGroup ");
        GluuGroup gluuGroup = copyUtils2.copy(group, null, false);
        if (gluuGroup == null) {
            throw new Exception("Scim2GroupService.createGroup(): Failed to create group; GluuGroup is null");
        }

        log.debug(" generating inum ");
        String inum = groupService.generateInumForNewGroup();

        log.debug(" getting DN ");
        String dn = groupService.getDnForGroup(inum);

        log.debug(" getting iname ");
        String iname = groupService.generateInameForNewGroup(group.getDisplayName().replaceAll(" ", ""));

        log.debug(" setting dn ");
        gluuGroup.setDn(dn);

        log.debug(" setting inum ");
        gluuGroup.setInum(inum);

        log.debug(" setting iname ");
        gluuGroup.setIname(iname);

        log.info("group.getMembers().size() : " + group.getMembers().size());
        if (group.getMembers().size() > 0) {
        	serviceUtil.personMembersAdder(gluuGroup, dn);
        }

        // As per spec, the SP must be the one to assign the meta attributes
        log.info(" Setting meta: create group ");
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
        Date dateCreated = DateTime.now().toDate();
        String relativeLocation = "/scim/v2/Groups/" + inum;
        gluuGroup.setAttribute("oxTrustMetaCreated", dateTimeFormatter.print(dateCreated.getTime()));
        gluuGroup.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateCreated.getTime()));
        gluuGroup.setAttribute("oxTrustMetaLocation", relativeLocation);

        // For custom script: create group
        if (externalScimService.isEnabled()) {
            externalScimService.executeScimCreateGroupMethods(gluuGroup);
        }

        log.debug("adding new GluuGroup");
        groupService.addGroup(gluuGroup);

        Group createdGroup = copyUtils2.copy(gluuGroup, null);

        return createdGroup;
    }

    public Group updateGroup(String id, Group group) throws Exception {
        GluuGroup gluuGroup = groupService.getGroupByInum(id);
        if (gluuGroup == null) {

            throw new EntryPersistenceException("Scim2GroupService.updateGroup(): " + "Resource " + id + " not found");

        } else {

            // Validate if attempting to update displayName of a different id
            if (gluuGroup.getDisplayName() != null) {

                GluuGroup groupToFind = new GluuGroup();
                groupToFind.setDisplayName(group.getDisplayName());

                List<GluuGroup> foundGroups = groupService.findGroups(groupToFind, 2);
                if (foundGroups != null && foundGroups.size() > 0) {
                    for (GluuGroup foundGroup : foundGroups) {
                        if (foundGroup != null && !foundGroup.getInum().equalsIgnoreCase(gluuGroup.getInum())) {
                            throw new DuplicateEntryException("Cannot update displayName of a different id: " + group.getDisplayName());
                        }
                    }
                }
            }
        }

        GluuGroup updatedGluuGroup = copyUtils2.copy(group, gluuGroup, true);

        if (group.getMembers().size() > 0) {
        	serviceUtil.personMembersAdder(updatedGluuGroup, groupService.getDnForGroup(id));
        }

        log.info(" Setting meta: update group ");
        DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime().withZoneUTC();  // Date should be in UTC format
        Date dateLastModified = DateTime.now().toDate();
        updatedGluuGroup.setAttribute("oxTrustMetaLastModified", dateTimeFormatter.print(dateLastModified.getTime()));
        if (updatedGluuGroup.getAttribute("oxTrustMetaLocation") == null || (updatedGluuGroup.getAttribute("oxTrustMetaLocation") != null && updatedGluuGroup.getAttribute("oxTrustMetaLocation").isEmpty())) {
            String relativeLocation = "/scim/v2/Groups/" + id;
            updatedGluuGroup.setAttribute("oxTrustMetaLocation", relativeLocation);
        }

        // For custom script: update group
        if (externalScimService.isEnabled()) {
            externalScimService.executeScimUpdateGroupMethods(updatedGluuGroup);
        }

        groupService.updateGroup(updatedGluuGroup);

        log.debug(" group updated ");

        Group updatedGroup = copyUtils2.copy(updatedGluuGroup, null);

        return updatedGroup;
    }

    public void deleteGroup(String id) throws Exception {
        log.info(" Checking if the group exists ");
        log.info(" id : " + id);

        GluuGroup gluuGroup = groupService.getGroupByInum(id);
        if (gluuGroup == null) {

            log.info(" the group is null ");
            throw new EntryPersistenceException("Scim2GroupService.deleteGroup(): " + "Resource " + id + " not found");

        } else {

            // For custom script: delete group
            if (externalScimService.isEnabled()) {
                externalScimService.executeScimDeleteGroupMethods(gluuGroup);
            }

            log.info(" getting started to delete members from groups ");
            if (gluuGroup.getMembers() != null) {

                if (gluuGroup.getMembers().size() > 0) {

                    log.info(" getting dn for group ");
                    String dn = groupService.getDnForGroup(id);
                    log.info(" DN : " + dn);

                    serviceUtil.deleteGroupFromPerson(gluuGroup, dn);
                }
            }

            log.info(" removing the group ");
            groupService.removeGroup(gluuGroup);
        }
    }
}
