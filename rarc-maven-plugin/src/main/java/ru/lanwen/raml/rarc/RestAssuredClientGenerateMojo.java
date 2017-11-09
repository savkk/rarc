package ru.lanwen.raml.rarc;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author lanwen (Merkushev Kirill)
 */
@Mojo(name = "generate-client", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
@Execute(goal = "generate-client")
public class RestAssuredClientGenerateMojo extends AbstractMojo {
    private final Logger LOG = LoggerFactory.getLogger(RestAssuredClientGenerateMojo.class);
    @Parameter(defaultValue = "${project.build.resources[0].directory}/raml/")
    private String ramlDirPath;

    @Parameter(required = true)
    private String basePackage;

    @Parameter(defaultValue = "${project.build.directory}/generated-sources/raml")
    private String outputDir;

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Path ramlFolder = Paths.get(ramlDirPath);
            if (!ramlFolder.toFile().exists()) {
                throw new IllegalArgumentException("Could not found folder " + ramlDirPath);
            }

            File inputDir = ramlFolder.toFile();

            File[] files = inputDir.listFiles();
            if (files == null || files.length == 0) {
                LOG.error("Can't find any directory with API");
                return;
            }

            Arrays.stream(files)
                    .filter(File::isDirectory)
                    .forEach(apiDirectory -> {
                        Path apiPath = Paths.get(apiDirectory.getAbsolutePath() + "/api.raml");
                        LOG.info("RAML: {}", apiPath.toAbsolutePath());
                        if (!apiPath.toFile().exists()) {
                            LOG.error("File doesn't exist");
                            return;
                        }

                        new RestAssuredRamlCodegen(
                                CodegenConfig.codegenConf()
                                        .withInputPath(apiPath)
                                        .withBasePackage(basePackage)
                                        .withOutputPath(Paths.get(outputDir))
                        ).generate();

                    });

            project.addCompileSourceRoot(outputDir);
        } catch (Exception e) {
            throw new MojoExecutionException("Exception while generating client.", e);
        }
    }
}
