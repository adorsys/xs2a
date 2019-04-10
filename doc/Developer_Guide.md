# Developer Guide

## Migrating to a newer version of PSD2 API
For migration to a newer version of PSD2 API you should follow these steps: 

1. Open https://github.com/swagger-api/swagger-codegen and clone the repository.
2. Build the project with `mvn clean install`.
3. Copy the file with a new version of PSD2 API(e.g. `psd2-api-1.3.yaml`) to the `modules/swagger-codegen-cli/target` directory.
4. Apply some patches to the yaml if it's necessary.
5. Add the following file `config.json` to the `modules/swagger-codegen-cli/target`: 

    ```
    {
     "title”: "XS2A server api”,
     "basePackage”: "de.adorsys.psd2",
     "configPackage”: "de.adorsys.psd2",
     "modelPackage”: "de.adorsys.psd2.model”,
     "apiPackage”: "de.adorsys.psd2.api”,
     "interfaceOnly”: true,
     "dateLibrary” : "java8”,
     "java8": true,
     "implicitHeaders" : false,
     "delegatePattern" : true,
     "useTags": true
    }
    ```

6. Run the following command in the command line: `java -jar modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate -i {PSD2 API file} -c config.json -l spring -o psd2-api-spring --template-engine mustache`,
replacing `{PSD2 API file}` with the actual name of file.
7. Move the generated classes and interfaces from `psd2-api-spring` folder into appropriate packages and interfaces of `xs2a-server-api` module.
8. Manually fix generated classes by:
   - removing one of duplicated `_initiatePayment` methods in `de.adorsys.psd2.api.PaymentApi` with `Object` request body;
   - removing incorrect media types from `consumes` property of the `@RequestMapping` annotation on `_initiatePayment`
    methods in `de.adorsys.psd2.api.PaymentApi`: `_initiatePayment` method with `Object` request body should consume 
    only JSON bodies (`consumes = {"application/json"}`), method with `xml_sct` and `json_standingorderType` request 
    parameters should handle pain.001 XML bodies (`consumes = {"application/xml", "multipart/form-data"}`)
    and the method with `String` request body should consume raw payments, passed as plaintext (`consumes = {"text/plain"}`);
   - removing `@Size` annotation that are placed above enums in generated models (for now it's only applicable to
   `getDayOfExecution()` methods in models that are related to periodic payments);
