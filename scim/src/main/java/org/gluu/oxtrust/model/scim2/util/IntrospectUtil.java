package org.gluu.oxtrust.model.scim2.util;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.annotations.StoreReference;
import org.gluu.oxtrust.model.scim2.annotations.Validator;
import org.gluu.oxtrust.model.scim2.extensions.Extension;
import org.gluu.oxtrust.model.scim2.fido.FidoDeviceResource;
import org.gluu.oxtrust.model.scim2.provider.ResourceType;
import org.gluu.oxtrust.model.scim2.provider.ServiceProviderConfig;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.group.GroupResource;
import org.gluu.oxtrust.model.scim2.schema.SchemaResource;
import org.gluu.oxtrust.model.scim2.user.UserResource;

import javax.lang.model.type.NullType;
import javax.ws.rs.HeaderParam;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by jgomer on 2017-09-14.
 *
 * Provides miscelaneous routines to query classes/objects properties using reflection mechanisms. Additionally, this
 * class exposes some public members that contain useful information about SCIM resources that is gather upon class loading
 */
public class IntrospectUtil {

    private static Logger log = LogManager.getLogger(IntrospectUtil.class);

    /**
     * From a bidimensional Array of Annotations (usually obtained by a call to a Method's getParameterAnnotations), it
     * searches for an annotation pertaining to class HeaderParam and whose value equals "Authorization"
     * @param annotations A two-dimensional array of annotations
     * @return The index (on the first dimension) where the annotation is found if any, otherwise returns -1
     */
    public static int indexOfAuthzHeader(Annotation annotations[][]){

        int j=-1;

        for (int i = 0; i<annotations.length && j<0; i++){
            //Iterate over annotations found at every parameter
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof HeaderParam) {
                    //Verifies this is an authz header
                    if (((HeaderParam)annotation).value().equals("Authorization")) {
                        j=i;
                        break;
                    }
                }
            }
        }
        return j;

    }

    /**
     * This method will find a java Field with a particular name.  If  needed, this method will search through
     * super classes.  The field does not need to be public.
     * Adapted from https://github.com/pingidentity/scim2/blob/master/scim2-sdk-common/src/main/java/com/unboundid/scim2/common/utils/SchemaUtils.java
     *
     * @param cls the java Class to search.
     * @param fieldName the name of the field to find.
     * @return A Field object, or null if no field was found
     */
    private static Field findField(final Class<?> cls, final String fieldName){

        Class<?> currentClass = cls;

        while(currentClass != null){
            Field fields[] = currentClass.getDeclaredFields();
            for (Field field : fields){
                if(field.getName().equals(fieldName))
                    return field;
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;

    }

    public static Field findFieldFromPath(Class<?> initcls, String path){

        Class cls=initcls;
        Field f=null;

        for (String prop : path.split("\\.")) {
            f=findField(cls, prop);

            if (f!=null) {
                cls = f.getType();
                if (isCollection(cls)) {
                    Attribute attrAnnot = f.getAnnotation(Attribute.class);
                    if (attrAnnot != null)
                        cls = attrAnnot.multiValueClass();
                }
            }
            else
                break;
        }
        return f;

    }

    private static Method getGetter(String fieldName, Class clazz) throws Exception{
        PropertyDescriptor[] props = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
        for (PropertyDescriptor p : props)
            if (p.getName().equals(fieldName))
                return p.getReadMethod();
        return null;
    }

    public static boolean isCollection(Class clazz){
        return Collection.class.isAssignableFrom(clazz);
    }

    public static List<String> getPathsInExtension(Extension extension){

        List<String> list=new ArrayList<String>();
        for (String attr : extension.getFields().keySet())
            list.add(extension.getUrn() + "." + attr);
        return list;

    }

    private static List<String> requiredAttrsNames;
    private static List<String> defaultAttrsNames;
    private static List<String> alwaysAttrsNames;
    private static List<String> neverAttrsNames;
    private static List<String> validableAttrsNames;
    private static List<String> canonicalizedAttrsNames;

    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> requiredCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> defaultCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> alwaysCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> neverCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> validableCoreAttrs;
    public static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> canonicalCoreAttrs;

    public static Map<Class <? extends BaseScimResource>, SortedSet<String>> allAttrs;
    public static Map<Class <? extends BaseScimResource>, Map<String, String>> storeRefs;

    private static Map<Class<? extends BaseScimResource>, Map<String, List<Method>>> newEmptyMap(){
        return new HashMap<Class<? extends BaseScimResource>, Map<String, List<Method>>>();
    }

    private static void resetAttrNames(){
        requiredAttrsNames=new ArrayList<String>();
        defaultAttrsNames=new ArrayList<String>();
        validableAttrsNames=new ArrayList<String>();
        canonicalizedAttrsNames=new ArrayList<String>();
        alwaysAttrsNames=new ArrayList<String>();
        neverAttrsNames=new ArrayList<String>();
    }

    private static void resetMaps(){
        requiredCoreAttrs=newEmptyMap();
        defaultCoreAttrs=newEmptyMap();
        alwaysCoreAttrs=newEmptyMap();
        neverCoreAttrs=newEmptyMap();
        validableCoreAttrs=newEmptyMap();
        canonicalCoreAttrs=newEmptyMap();

        allAttrs=new HashMap<Class<? extends BaseScimResource>, SortedSet<String>>();
        storeRefs=new HashMap<Class<? extends BaseScimResource>, Map<String,String>>();
    }

    private static void traverseClassForNames(Class clazz, String prefix, List<Field> extraFields, boolean prune) throws Exception{

        List<Field> fields=new ArrayList<Field>();
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        fields.addAll(extraFields);

        for (Field f : fields){
            Attribute attrAnnot=f.getAnnotation(Attribute.class);
            if (attrAnnot!=null){

                String name=f.getName();
                if (prefix.length()>0)
                    name=prefix + "." + name;

                switch (attrAnnot.returned()){
                    case ALWAYS:
                        alwaysAttrsNames.add(name);
                        break;
                    case DEFAULT:
                        defaultAttrsNames.add(name);
                        break;
                    case NEVER:
                        neverAttrsNames.add(name);
                        break;
                }

                if (attrAnnot.isRequired())
                    requiredAttrsNames.add(name);

                if (attrAnnot.canonicalValues().length>0)
                    canonicalizedAttrsNames.add(name);

                Validator vAnnot=f.getAnnotation(Validator.class);
                if (vAnnot!=null)
                    validableAttrsNames.add(name);

                if (!prune && attrAnnot.type().equals(AttributeDefinition.Type.COMPLEX)) {
                    Class cls = attrAnnot.multiValueClass();  //Use <T> parameter of Collection if present
                    if (cls.equals(NullType.class))
                        cls=f.getType();

                    if (clazz.equals(cls))  //Prevent infinite loop
                        prune=true;

                    traverseClassForNames(cls.equals(NullType.class) ? f.getType() : cls, name, new ArrayList<Field>(), prune);
                }
            }
        }
    }

    private static Map<String, List<Method>> computeGettersMap(List<String> attrNames, Class baseClass) throws Exception{

        Map<String, List<Method>> map=new HashMap<String, List<Method>>();

        for (String attrName : attrNames) {
            List<Method> list =new ArrayList<Method>();
            Class clazz=baseClass;

            for (String prop : attrName.split("\\.")) {
                Method method=getGetter(prop, clazz);
                list.add(method);

                if (isCollection(method.getReturnType())) {  //Use class of parameter in collection
                    Field f=findField(clazz, prop);
                    Attribute attrAnnot=f.getAnnotation(Attribute.class);
                    if (attrAnnot!=null)
                        clazz=attrAnnot.multiValueClass();
                }
                else
                    clazz=method.getReturnType();
            }
            map.put(attrName, list);
        }
        return map;

    }

    static {
        try {
            List<Field> basicFields=Arrays.asList(BaseScimResource.class.getDeclaredFields());
            resetMaps();

            List<Class<? extends BaseScimResource>> resourceClasses=Arrays.asList(UserResource.class, GroupResource.class,
                    FidoDeviceResource.class, ServiceProviderConfig.class, ResourceType.class, SchemaResource.class);

            //Perform initializations needed for all resource types
            for (Class<? extends BaseScimResource> aClass : resourceClasses){
                resetAttrNames();

                traverseClassForNames(aClass, "", basicFields, false);
                requiredCoreAttrs.put(aClass, computeGettersMap(requiredAttrsNames, aClass));
                defaultCoreAttrs.put(aClass, computeGettersMap(defaultAttrsNames, aClass));
                alwaysCoreAttrs.put(aClass, computeGettersMap(alwaysAttrsNames, aClass));
                neverCoreAttrs.put(aClass, computeGettersMap(neverAttrsNames, aClass));
                validableCoreAttrs.put(aClass, computeGettersMap(validableAttrsNames, aClass));
                canonicalCoreAttrs.put(aClass, computeGettersMap(canonicalizedAttrsNames, aClass));

                allAttrs.put(aClass, new TreeSet<String>());
                allAttrs.get(aClass).addAll(alwaysAttrsNames);
                allAttrs.get(aClass).addAll(defaultAttrsNames);
                allAttrs.get(aClass).addAll(neverAttrsNames);
            }

            for (Class<? extends BaseScimResource> cls : resourceClasses) {
                //This is a map from attributes to storage references (e.g. LDAP attributes)
                Map<String, String> map = new HashMap<String, String>();

                for (String attrib : allAttrs.get(cls)) {
                    Field field = findFieldFromPath(cls, attrib);
                    if (field != null) {
                        StoreReference annotation = field.getAnnotation(StoreReference.class);
                        if (annotation != null) {
                            if (StringUtils.isNotEmpty(annotation.ref()))
                                map.put(attrib, annotation.ref());
                            else {
                                List<Class<? extends BaseScimResource>> clsList = Arrays.asList(annotation.resourceType());
                                int i = clsList.indexOf(cls);
                                if (i>=0 && i<annotation.refs().length)
                                    map.put(attrib, annotation.refs()[i]);
                            }
                        }
                    }
                }
                storeRefs.put(cls, map);
            }
        }
        catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

}