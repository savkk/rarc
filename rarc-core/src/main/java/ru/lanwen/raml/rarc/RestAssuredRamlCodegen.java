package ru.lanwen.raml.rarc;

import org.raml.model.Raml;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.lanwen.raml.rarc.api.ra.ReqSpecField;
import ru.lanwen.raml.rarc.api.ra.root.NestedConfigClass;
import ru.lanwen.raml.rarc.api.ra.root.ReqSpecSupplField;
import ru.lanwen.raml.rarc.api.ra.root.RootApiClase;
import ru.lanwen.raml.rarc.rules.ResourceClassBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author lanwen (Merkushev Kirill)
 */
public class RestAssuredRamlCodegen {
    private final Logger LOG = LoggerFactory.getLogger(RestAssuredRamlCodegen.class);
    private final CodegenConfig codegenConfig;

    public RestAssuredRamlCodegen(CodegenConfig config) {
        this.codegenConfig = config;
    }

    public void generate() {
        Path path = codegenConfig.getInputPath();
        LOG.info("RAML: {}", path.toAbsolutePath());
        LOG.info("Output path: {}", codegenConfig.getOutputPath().toAbsolutePath());
        if (path.endsWith(".raml")) {
            LOG.warn("Wrong path - should end with .raml");
            return;
        }

        Raml raml = new RamlDocumentBuilder(new FileResourceLoader(path.getParent().toFile()))
                .build(path.getFileName().toString());

        ReqSpecSupplField baseReqSpec = new ReqSpecSupplField();
        ReqSpecField req = new ReqSpecField();

        try {
            new RootApiClase(new NestedConfigClass(raml.getTitle(), baseReqSpec, req))
                    .javaFile(raml, codegenConfig.getBasePackage())
                    .writeTo(codegenConfig.getOutputPath());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read or write files");
        }

        raml.getResources().values().parallelStream().forEach(resource -> {
            new ResourceClassBuilder().withCodegenConfig(codegenConfig).withResource(resource).withReq(req).generate();
        });
    }
}
