T### Expert-Level Prompt

---

**Prompt Title**: Implement VopController API Request Mapping Using PaymentController Principles

---

**Objective**:
You will analyze the `PaymentController` class to understand its structure and principles for handling API requests and mapping them to domain requests. You will then apply the same patterns to the `VopController` class, specifically for handling `verificationOfParty` and `bulkVerificationOfParty` API requests. This includes mapping specific request parameters to `signingMsg`, `signingBody`, and `otherParams` objects.

Your task involves the following:

1. **Analysis**:

    * Analyze the `PaymentController` implementation to identify the patterns used for mapping API request data to domain objects.
    * Pay close attention to how the `paymentInitiationParameters` is handled and the logic behind its mapping into domain objects (like `signingMsg`, `signingBody`, etc.).
    * Identify the principles used to transform and store the API request data into domain-specific structures in the `PaymentController`.

2. **Create Checklist**:

    * Based on your analysis of the `PaymentController`, create a checklist of tasks that must be completed for the `VopController`. This checklist will guide you step-by-step through the process of implementing the required features in the `VopController`.
    * Store this checklist in a markdown file in the path `prompts/implementation/vopControllerMapping.md`.

3. **Iterative Implementation**:

    * For each task in the checklist, iterate through the steps and implement the necessary changes one at a time, ensuring that the pattern from the `PaymentController` is followed.
    * After completing each task, document what you did in a new markdown file (e.g., `prompts/implementation/vopControllerMapping_<task_name>.md`), explaining the implementation details and how the `PaymentController` principles were applied.

4. **Create Mappers/DTOs**:

    * For the `VopController`, create the necessary mappers and DTOs designed to transform the API request into domain objects used within the service layer.
    * Specifically, create the following mappers/DTOs:

        * A mapper to transform the `xJwsSignature`, `digest`, and related data into the `signingMsg` object.
        * A mapper to transform the `bodySigProfile`, `bodyEncProfile`, and `bodyEncList` into the `signingBody` object.
        * A mapper for the remaining parameters, which will be stored in the `otherParams` object.
    * Ensure that each of these mappers follows the structure and principles found in the `PaymentController` implementation.

5. **Testing/Verification**:

    * After implementing the mappings, verify that the transformation is working as expected. Although you're not implementing the service layer at this time, ensure that the mappings are correctly structured and transform the data as required.

---

**Input Files to Analyze**:

* `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/PaymentController.java`
* `xs2a-impl/src/main/java/de/adorsys/psd2/xs2a/web/controller/psd2/VopController.java`

---

### **Checklist for VopController Mapping Implementation (to be stored in `prompts/implementation/vopControllerMapping.md`)**

1. **Analyze PaymentController**:

    * Examine how the `paymentInitiationParameters` object is populated.
    * Identify how `signingMsg`, `signingBody`, and `otherParams` are structured and populated.
    * Understand the method of extracting parameters and how they are mapped.

2. **Create Mapper for `signingMsg`**:

    * Extract the relevant parameters from the API request (e.g., `xJwsSignature`, `digest`).
    * Create a DTO or model for the `signingMsg` object.
    * Implement the mapping logic that takes these parameters and populates the `signingMsg`.

3. **Create Mapper for `signingBody`**:

    * Extract the relevant parameters from the API request (e.g., `bodySigProfile`, `bodyEncProfile`, `bodyEncList`).
    * Create a DTO or model for the `signingBody` object.
    * Implement the mapping logic that takes these parameters and populates the `signingBody`.

4. **Create Mapper for `otherParams`**:

    * Identify all other parameters in the API request.
    * Create a DTO or model for the `otherParams` object.
    * Implement the mapping logic that takes these parameters and stores them in `otherParams`.

5. **Create DTOs for `signingMsg`, `signingBody`, and `otherParams`**:

    * Design DTOs that reflect the structure needed for the `signingMsg`, `signingBody`, and `otherParams`.
    * Ensure the DTOs are appropriately structured to handle the incoming API request data.

6. **Implement VopController API Logic**:

    * Implement the logic to call the mappers for each parameter group in the `VopController`.
    * Ensure the mappings are done before calling any downstream service methods.

7. **Document Implementation**:

    * After each task, document your implementation in a separate markdown file detailing the changes made and how the logic from `PaymentController` was reused.

---

**Expected Outputs**:

* A series of markdown files, each documenting the tasks completed and the steps followed. Example:

    * `prompts/implementation/vopControllerMapping.md`
    * `prompts/implementation/vopControllerMapping_mapper_signingMsg.md`
    * `prompts/implementation/vopControllerMapping_mapper_signingBody.md`
    * `prompts/implementation/vopControllerMapping_mapper_otherParams.md`

---

**Final Step**:
Once all tasks are complete, ensure that the new logic for mapping the API request to domain requests in the `VopController` is consistent with the patterns and principles followed in the `PaymentController`.


