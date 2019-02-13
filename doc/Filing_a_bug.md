# How to report an issue to XS2A Team

If you think that our system behaves in an unexpected way or incorrect, please raise a bug ticket,
following this process:

1. Fill in the template to bug-ticket, try to be as precise as possible.
The more information you provide, the faster analysis would be possible.
2. Mark your ticket with following labels (if applicable):
**`Bug`** - to point out the type of ticket;
**`Urgent`** - only if this problem blocks your further work and no workaround is possible (Please describe accordingly why)
3. Please assign your ticket to `@vhaex` (Gitlab tickets) or leave it unassigned on Github.

# Bug ticket workflow

## Ticket preanalysis

After creation XS2A Team analyses information you provided 
and gives a first conclusion as a comment in the ticket, mentioning ticket's reporter (i.o. to get him notified).

If ticket gets accepted for a fix, it becomes a label **`Confirmed`**.

If XS2A Team considers the problem not reproducible or not valid, ticket will be labeled with **`Rejected`**.

## Ticket planning (Accepted tickets)

XS2A Team processes Tickets in scope of sprints according to versions affected.
Mainline, like **`2.x`**, **`3.x`** will be set also as a label to mark for which version a fix is to be provided.

If XS2A Team delivers a Hotfix version, a **`HOTFIX`** label will be set.
Planning ticket to sprints is done using the [Roadmap](roadmap.md).

## Bugfixing (Accepted tickets)

When XS2A Team fixes a bug and it goes through internal quality gates, XS2A would notify reporter using the comment section in the ticket
(with reporter's login, like `@dgo`) to retest it with the development version (if possible).

## Release (Accepted tickets)

After release of new version(s) containing the bugfix, ticket reporter is asked to provide feedback in the ticket, by commenting out if fix was successful or pointing out the problems that still exist and assigning ticket back to `@vhaex` (Gitlab) 
