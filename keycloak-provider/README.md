```
mvn clean install

docker build -t keycloak 

docker run -p 8080:8080 -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin123 keycloak

curl -X POST \
  http://localhost:8080/auth/realms/demobank/clients-registrations/openid-connect \
  -H 'content-type: application/json' \
  -d '{
	"client_name":"XS2A Client",
	"redirect_uris":["*"]
}’

{
    "redirect_uris": [
        "*"
    ],
    "token_endpoint_auth_method": "client_secret_basic",
    "grant_types": [
        "authorization_code",
        "refresh_token"
    ],
    "response_types": [
        "code",
        "none"
    ],
    "client_id": "9fee4b6e-6216-4e48-8839-dec2de38ac22",
    "client_secret": "bd270b52-b718-4eb0-ae68-734a6d9e18b0",
    "client_name": "XS2A Client",
    "subject_type": "public",
    "client_id_issued_at": 1520334677,
    "client_secret_expires_at": 0,
    "registration_client_uri": "http://localhost:8080/auth/realms/demobank/clients-registrations/openid-connect/9fee4b6e-6216-4e48-8839-dec2de38ac22",
    "registration_access_token": "eyJhbGciOiJSUzI1NiIsImtpZCIgOiAiRGg5NmZTZ2pLZnQyLXl4RFlSWmhPQy1TbFpoUGU2akRFdGxwTnplanJSYyJ9.eyJqdGkiOiI4NDMzYzY1Ny1kMjM0LTQ1MTUtOTZlYS0zZGE4MWJlMzNjYjYiLCJleHAiOjAsIm5iZiI6MCwiaWF0IjoxNTIwMzM0Njc3LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvZGVtb2JhbmsiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvZGVtb2JhbmsiLCJ0eXAiOiJSZWdpc3RyYXRpb25BY2Nlc3NUb2tlbiIsInJlZ2lzdHJhdGlvbl9hdXRoIjoiYW5vbnltb3VzIn0.grel1LxPLJSeCilpxn09MLcllPqV2__0zPvM3VUypJV6q1cuEYqLaiLSI5oNPnXrcVIjS9Kko8ZnmmrxxOB157TGd2ZETJpUU7np5Kieo-k6-woxA4rOYmfLQuIj8MfhBY8rPnioMyWsN5Cg090aHkz_SMzFp9Qb8IDUEzUV6YGuZwllMErqDPg5_3GMYqYOs1yLyDDVAs0p4qF3pxS5StC4bw9abI_BRv2ouO_k9G_z0x6G7EzwTBu9-8slPwUyb4SPDYpyMADA57BcnCvu3D-G77Wrqkz_x18KaZG_UGk6DcgCfwEiX4Fr1p8YrMJh3QsP-EHejEI4-5MoEy3ajg"
}

```
