# Release notes v. 1.13

## Add parameter "deltaReportSupported" to ASPSP-profile
Now we can set parameter for delta-report support in ASPSP-Profile.

| Option                                  | Meaning                                                                                             | Default value                                        | Possible values                                                                                      |
|-----------------------------------------|-----------------------------------------------------------------------------------------------------|------------------------------------------------------|------------------------------------------------------------------------------------------------------|
|deltaReportSupported                     | This field indicates if an ASPSP supports Delta reports for transaction details                     | false                                                | true, false                                                                                          |

## Payment cancellation endpoint gives an error when trying to cancel finalised payments

When payment is finished (has transaction statuses *Cancelled, Rejected, AcceptedSettlementCompleted*) there is no possibility to cancel it or to proceed payment cancellation authorisation flow.


