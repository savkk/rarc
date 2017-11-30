package ru.lanwen.raml.rarc.rules;

import org.raml.model.MimeType;

import static ru.lanwen.raml.rarc.api.ApiResourceClass.addedObjectPackage;
import static ru.lanwen.raml.rarc.api.ra.AddJsonBodyMethod.bodyMethod;
import static ru.lanwen.raml.rarc.rules.BodyRule.MimeTypeEnum.byMimeType;

/**
 * Created by stassiak
 */
public class JsonBodyRule implements Rule<MimeType> {

    private boolean shortJson;

    public boolean isShortJson() {
        return shortJson;
    }

    public void setShortJson(boolean shortJson) {
        this.shortJson = shortJson;
    }

    @Override
    public void apply(MimeType body, ResourceClassBuilder resourceClassBuilder) {
        if (byMimeType(body) == BodyRule.MimeTypeEnum.JSON) {
            resourceClassBuilder.getApiClass().withMethod(
                    bodyMethod()
                            .withSchema(body.getCompiledSchema())
                            .withShortJson(shortJson)
                            .withExample(body.getExample())
                            .withReqName(resourceClassBuilder.getReq().name())
                            .withInputPathForJsonGen(resourceClassBuilder.getCodegenConfig().getInputPath().getParent())
                            .withOutputPathForJsonGen(resourceClassBuilder.getCodegenConfig().getOutputPath())
                            .withPackageForJsonGen(resourceClassBuilder.getCodegenConfig().getBaseObjectsPackage()
                                    + addedObjectPackage(body.getCompiledSchema().toString()))
                            .returns(resourceClassBuilder.getApiClass().name()));
        }

    }
}
