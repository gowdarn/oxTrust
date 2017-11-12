package org.gluu.oxtrust.model.scim2.schema;

import org.gluu.oxtrust.model.scim2.AttributeDefinition;
import org.gluu.oxtrust.model.scim2.BaseScimResource;
import org.gluu.oxtrust.model.scim2.annotations.Attribute;
import org.gluu.oxtrust.model.scim2.annotations.Schema;

import java.util.List;

/**
 * Created by jgomer on 2017-10-13.
 */
@Schema(id="urn:ietf:params:scim:schemas:core:2.0:Schema", name="Schema", description = "See section 7 RFC 7643")
public class SchemaResource extends BaseScimResource {

    @Attribute(description = "The schema's human-readable name",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private String name;

    @Attribute(description = "The schema's human-readable description",
            mutability = AttributeDefinition.Mutability.READ_ONLY)
    private String description;

    @Attribute(description = "A complex type that defines service provider attributes and their qualities",
            mutability = AttributeDefinition.Mutability.READ_ONLY,
            type = AttributeDefinition.Type.COMPLEX,
            multiValueClass = SchemaAttribute.class,
            isRequired = true)
    private List<SchemaAttribute> attributes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<SchemaAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<SchemaAttribute> attributes) {
        this.attributes = attributes;
    }

}