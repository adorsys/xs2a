# Release notes v.3.1

## Feature: Added new parameter for new Redirect SCA Approach
A new `scaRedirectFlow` parameter has been added to ASPSP profile.

| Option          | Meaning                                                             | Default value | Possible values |
|-----------------|---------------------------------------------------------------------|---------------|-----------------|
| scaRedirectFlow | This field indicates what variant of Redirect approach will be used | REDIRECT      | REDIRECT, OAUTH |

## Feature: Changed links for new Redirect SCA Approach subtype
Redirect SCA approach was extended: now it has two possible subtypes - `REDIRECT` (default) and `OAUTH`.
Depending on this subtype, the link names would be different in the following requests:
 - payment initiation
 - consent creation
 - payment cancellation authorisation

In case of OAUTH subtype scaOAuth link will be used instead of scaRedirect.
