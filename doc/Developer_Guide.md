# Developer Guide

## Migrating to a newer version of PSD2 API
For migration to a newer version of PSD2 API you should follow these steps: 

1. Open https://github.com/swagger-api/swagger-codegen and clone the repository.
2. Build the project with `mvn clean install`.
3. Copy the file with a new version of PSD2 API(e.g. `psd2-api-1.3.yaml`) to the `swagger-codegen-cli/target` directory.
4. Add the following file `config.json` to directory `swagger-codegen-cli/target`: 

    ```{
     “title”: “XS2A server api”,
     “basePackage”: “de.adorsys.psd2",
     “configPackage”: “de.adorsys.psd2",
     “modelPackage”: “de.adorsys.psd2.model”,
     “apiPackage”: “de.adorsys.psd2.api”,
     “interfaceOnly”: true,
     “dateLibrary” : “java8”,
     “java8": true,
     “implicitHeaders” : false,
     “delegatePattern” : true
    }
    ```

5. Run the following command in the command line: `java -jar swagger-codegen-cli.jar generate -i {PSD2 API file} -c config.json -l spring -o psd2-api-spring --template-engine mustache`.
6. You will get generated classes and interfaces in the `psd2-api-spring` folder.

