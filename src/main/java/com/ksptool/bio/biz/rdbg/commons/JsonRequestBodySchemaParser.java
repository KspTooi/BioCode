package com.ksptool.bio.commons.utils;

import com.ksptool.bio.commons.model.BodySchema;
import com.ksptool.bio.commons.model.BodySchemaParam;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JsonRequestBodySchemaParser {

    private JsonRequestBodySchemaParser() {
    }

    public static BodySchema parse(Operation operation) {
        if (operation == null) {
            return null;
        }
        return parse((OpenAPI) null, operation.getRequestBody());
    }

    public static BodySchema parse(OpenAPI openAPI, Operation operation) {
        if (operation == null) {
            return null;
        }
        return parse(openAPI, operation.getRequestBody());
    }

    public static BodySchema parse(RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }
        Content content = requestBody.getContent();
        if (content == null || content.isEmpty()) {
            return null;
        }
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = firstMediaType(content);
        }
        if (mediaType == null) {
            return null;
        }
        Schema<?> schema = mediaType.getSchema();
        if (schema == null) {
            return null;
        }
        String name = resolveSchemaName(schema);
        BodySchema bodySchema = buildBodySchema(name, schema, null);
        return bodySchema;
    }

    public static BodySchema parse(OpenAPI openAPI, RequestBody requestBody) {
        if (requestBody == null) {
            return null;
        }
        Content content = requestBody.getContent();
        if (content == null || content.isEmpty()) {
            return null;
        }
        MediaType mediaType = content.get("application/json");
        if (mediaType == null) {
            mediaType = firstMediaType(content);
        }
        if (mediaType == null) {
            return null;
        }
        Schema<?> schema = mediaType.getSchema();
        if (schema == null) {
            return null;
        }
        String name = resolveSchemaName(schema);
        BodySchema bodySchema = buildBodySchema(name, schema, openAPI);
        return bodySchema;
    }

    private static MediaType firstMediaType(Content content) {
        for (Map.Entry<String, MediaType> entry : content.entrySet()) {
            if (entry == null) {
                continue;
            }
            if (entry.getValue() == null) {
                continue;
            }
            return entry.getValue();
        }
        return null;
    }

    private static BodySchema buildBodySchema(String schemaName, Schema<?> schema, OpenAPI openAPI) {
        if (schema == null) {
            return null;
        }
        Schema<?> effective = dereference(openAPI, schema);
        BodySchema bodySchema = new BodySchema();
        String preferred = resolveSchemaName(schema);
        String finalName = StringUtils.isNotBlank(preferred) ? preferred : schemaName;
        bodySchema.setSchemaName(StringUtils.isNotBlank(finalName) ? finalName : null);

        List<BodySchemaParam> params = new ArrayList<>();

        if (effective instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) effective;
            Schema<?> itemSchema = dereference(openAPI, arraySchema.getItems());
            BodySchemaParam param = new BodySchemaParam();
            param.setName(StringUtils.isNotBlank(schemaName) ? schemaName : "items");
            param.setIsArray(true);
            String itemType = inferType(itemSchema);
            param.setType(itemType);
            param.setDescription(itemSchema != null ? itemSchema.getDescription() : null);
            Object defVal = itemSchema != null ? itemSchema.getDefault() : null;
            param.setDefaultValue(defVal != null ? String.valueOf(defVal) : null);
            param.setRequired(null);
            String nestedName = resolveSchemaName(itemSchema);
            BodySchema nested = buildBodySchema(nestedName, itemSchema, openAPI);
            param.setSchema(nested);
            params.add(param);
            bodySchema.setParams(params);
            return bodySchema;
        }

        Map<String, Schema> properties = effective.getProperties();
        if (properties != null && !properties.isEmpty()) {
            List<String> requiredList = effective.getRequired();
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                String name = entry.getKey();
                Schema<?> propertySchema = dereference(openAPI, entry.getValue());
                if (StringUtils.isBlank(name)) {
                    continue;
                }
                BodySchemaParam param = new BodySchemaParam();
                param.setName(name);
                boolean isArray = propertySchema instanceof ArraySchema;
                param.setIsArray(isArray);
                String type = inferType(propertySchema);
                param.setType(type);
                param.setDescription(propertySchema.getDescription());
                Object defVal = propertySchema.getDefault();
                param.setDefaultValue(defVal != null ? String.valueOf(defVal) : null);
                boolean isRequired = requiredList != null && requiredList.contains(name);
                param.setRequired(isRequired);

                if (isArray) {
                    Schema<?> itemSchema = dereference(openAPI, ((ArraySchema) propertySchema).getItems());
                    String nestedName = resolveSchemaName(itemSchema);
                    BodySchema nested = buildBodySchema(nestedName, itemSchema, openAPI);
                    param.setSchema(nested);
                }
                if (!(propertySchema instanceof ArraySchema)) {
                    Map<String, Schema> nestedProps = propertySchema.getProperties();
                    if (nestedProps != null && !nestedProps.isEmpty()) {
                        String nestedName = resolveSchemaName(propertySchema);
                        BodySchema nested = buildBodySchema(nestedName, propertySchema, openAPI);
                        param.setSchema(nested);
                    }
                }
                params.add(param);
            }
            bodySchema.setParams(params);
            return bodySchema;
        }

        BodySchemaParam selfParam = new BodySchemaParam();
        selfParam.setName(StringUtils.isNotBlank(schemaName) ? schemaName : "value");
        selfParam.setType(inferType(effective));
        selfParam.setDescription(effective.getDescription());
        Object defVal = effective.getDefault();
        selfParam.setDefaultValue(defVal != null ? String.valueOf(defVal) : null);
        selfParam.setRequired(null);
        selfParam.setIsArray(false);
        selfParam.setSchema(null);
        params.add(selfParam);
        bodySchema.setParams(params);
        return bodySchema;
    }

    private static String inferType(Schema<?> schema) {
        if (schema == null) {
            return null;
        }
        if (schema instanceof ArraySchema) {
            return "array";
        }
        String type = schema.getType();
        if (StringUtils.isNotBlank(type)) {
            return type;
        }
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null && !properties.isEmpty()) {
            return "object";
        }
        String format = schema.getFormat();
        if (StringUtils.isNotBlank(format)) {
            return format;
        }
        return "string";
    }

    private static String resolveSchemaName(Schema<?> schema) {
        if (schema == null) {
            return null;
        }
        String ref = schema.get$ref();
        if (StringUtils.isNotBlank(ref)) {
            int idx = ref.lastIndexOf('/');
            if (idx >= 0 && idx < ref.length() - 1) {
                return ref.substring(idx + 1);
            }
            return ref;
        }
        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            String itemsName = resolveSchemaName(items);
            if (StringUtils.isNotBlank(itemsName)) {
                return itemsName;
            }
        }
        String name = schema.getName();
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        String type = schema.getType();
        if (StringUtils.isNotBlank(type)) {
            return type;
        }
        String format = schema.getFormat();
        if (StringUtils.isNotBlank(format)) {
            return format;
        }
        return null;
    }

    private static Schema<?> dereference(OpenAPI openAPI, Schema<?> schema) {
        if (schema == null) {
            return null;
        }
        String ref = schema.get$ref();
        if (StringUtils.isBlank(ref)) {
            return schema;
        }
        if (openAPI == null) {
            return schema;
        }
        if (openAPI.getComponents() == null) {
            return schema;
        }
        if (openAPI.getComponents().getSchemas() == null) {
            return schema;
        }
        int idx = ref.lastIndexOf('/');
        String key = idx >= 0 && idx < ref.length() - 1 ? ref.substring(idx + 1) : ref;
        Schema<?> resolved = openAPI.getComponents().getSchemas().get(key);
        if (resolved != null) {
            return resolved;
        }
        return schema;
    }
}
