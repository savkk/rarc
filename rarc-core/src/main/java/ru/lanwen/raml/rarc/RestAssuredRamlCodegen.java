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
        File inputDir = path.toFile();

        LOG.info("RAML: {}", path.toAbsolutePath());

        if (!inputDir.exists()) {
            LOG.error("Directory doesn't exist");
            return;
        }

        File[] files = inputDir.listFiles();
        if (files == null || files.length == 0) {
            LOG.error("Can't find any directory with API");
            return;
        }

        String basePackage = codegenConfig.getBasePackage();
        Path outputPath = codegenConfig.getOutputPath();

        Arrays.stream(files)
                .filter(File::isDirectory)
                .forEach(apiDirectory -> {
                    Path apiPath = Paths.get(apiDirectory.getAbsolutePath() + "/api.raml");
                    LOG.info("RAML: {}", apiPath.toAbsolutePath());

                    if (!apiPath.toFile().exists()) {
                        LOG.error("File doesn't exist");
                        return;
                    }

                    Raml raml = new RamlDocumentBuilder(new FileResourceLoader(apiPath.getParent().toFile()))
                            .build(apiPath.getFileName().toString());

                    ReqSpecSupplField baseReqSpec = new ReqSpecSupplField();
                    ReqSpecField req = new ReqSpecField();

                    codegenConfig
                            .withBasePackage(basePackage + "." + apiDirectory.getName())
                            .withOutputPath(Paths.get(outputPath + "/" + apiDirectory.getName()));
                    try {
                        new RootApiClase(new NestedConfigClass(raml.getTitle(), baseReqSpec, req))
                                .javaFile(raml, codegenConfig.getBasePackage())
                                .writeTo(codegenConfig.getOutputPath());
                    } catch (IOException e) {
                        LOG.error("Не удалось записать файл " + codegenConfig.getOutputPath().toAbsolutePath(), e);
                        return;
                    }

                    raml.getResources().values().parallelStream().forEach(resource -> {
                        new ResourceClassBuilder().withCodegenConfig(codegenConfig).withResource(resource).withReq(req).generate();
                    });
                });
    }
}
