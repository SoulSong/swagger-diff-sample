package com.shf;

import com.deepoove.swagger.diff.SwaggerDiff;
import com.deepoove.swagger.diff.output.HtmlRender;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author songhaifeng
 */
@SpringBootApplication
@RestController
@Api(description = "Test")
@Slf4j
public class SwaggerDiffApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwaggerDiffApplication.class, args);
    }

    @ApiOperation(value = "foo.test", notes = "This is a test api.", consumes = MediaType.APPLICATION_JSON_VALUE,
            extensions = @Extension(properties = {@ExtensionProperty(name = "ext_prop_1", value = "prop_1"),
                    @ExtensionProperty(name = "ext_prop_2", value = "prop_2")}))
    @PostMapping(value = "/foo/test", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public FooResponse foo(@RequestBody FooRequest fooRequest) {
        return FooResponse.builder().name(fooRequest.getName()).age(12).password("123").country(fooRequest.getCountry()).build();
    }

    @ApiOperation(value = "swagger-diff", notes = "Generate the `swagger-diff.html`.")
    @GetMapping("/diff")
    public void diff() throws IOException {
        SwaggerDiff diff = SwaggerDiff.compareV2("/api-v1.json", "http://127.0.0.1:8080/v2/api-docs");
        String html = new HtmlRender().render(diff);
        try (FileWriter fw = new FileWriter(
                "/diff.html");) {
            fw.write(html);
        }
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel
    private static class FooResponse {
        @ApiModelProperty
        private String name;
        @ApiModelProperty
        private int age;
        @ApiModelProperty
        private String country;
        @ApiModelProperty(hidden = true)
        @JsonIgnore
        private String password;

    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel
    private static class FooRequest {
        private String name;
        private String country;
    }


    @Configuration
    @EnableSwagger2
    class SwaggerConfiguration {

        @Bean
        public Docket api() {
            return new Docket(DocumentationType.SWAGGER_2)
                    .genericModelSubstitutes(ResponseEntity.class)
                    .useDefaultResponseMessages(false)
                    .forCodeGeneration(true)
                    .pathMapping("/")
                    .select()
                    // exclude feign-client
                    .apis(RequestHandlerSelectors.basePackage("com.shf"))
                    .paths(PathSelectors.any())
                    .build()
                    .apiInfo(new ApiInfoBuilder().title("swagger diff demo")
                                    .description("swagger diff demo")
                                    .contact(new Contact("songhaifeng", "", "songhaifengshuaige@gmail.com"))
                                    .license("Apache 2.0")
                                    .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                                    .version("1.1.0")
                                    .build()
                    )
                    .globalOperationParameters(
                            Collections.singletonList(
                                    new ParameterBuilder()
                                            .name("Authorization")
                                            .description("accessToken")
                                            .modelRef(new ModelRef("string"))
                                            .parameterType("header")
                                            .required(false)
                                            .defaultValue("Bearer ")
                                            .order(1)
                                            .build()
                            )
                    );
        }

    }
}
