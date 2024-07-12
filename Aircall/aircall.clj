(config
(
    text-field 
    :name "clientId"
    :label "Client-Id"
    :placeholder "Enter your Client Id"
    :required "true"
)

(
    password-field
    :name "clientSecret"
    :label "Client-Secret"
    :placeholder "Enter your Client Secret"
    :required "true"
)

(
    oauth2/authorization-code-with-client-credentials
    (authorization-code
        (source
        (http/get
        :base-url ""
        :url "https://dashboard.aircall.io/oauth/authorize"
        (query-params
        "response_type" "code"
        "client_id" "{clientId}"
        "redirect_uri" "$FIVETRAN-APP-URL/integrations/aircall/oauth2/return" 
        "scope" "public_api"))))

    (access-token
        (source
        (http/post
        :base-url ""
        :url "https://api.aircall.io/v1/oauth2/token"
        (body-params
        "code" "$AUTHORISATION-CODE"
        "client_id" "{clientId}"
        "client_secret" "{clientSecret}"
        "grant_type" "authorization_code"
        "redirect_uri" "$FIVETRAN-APP-URL/integrations/aircall/oauth2/return")))

    (fields 
    access-token :<= "access_token"
    refresh-token :<= "refresh_token"
    token-type :<= "token_type"
    scope
    realm-id 
    expires-at :<= "expires_at"

    ))

))

(default-source
(
    http/get:base-url "https://api.aircall.io/v1"
    (header-params "Accept" "application/json") 
    (auth/oauth2)
    (paging//url-key :url-value-path-in-response "next_page_link")
    (error-handler
    (when :status 404 :message "not found" :action fail)
    (when :status 400 :message "Bad Request" :action fail)
    (when :status 429 :action (rate-limit :header-param-key "X-AircallApi-Reset"
    (timestamp/absolute (format "epoch-sec"))))
    (when :status 403 :message "Forbidden" )
    (when :status 405 :message "Method Not Allowed" )
    (when :status 500 :action (sleep 60))
    (when :status 500 :message "retry" :action (retry 6)))
))

(temp-entity TAG
    (api-docs-url "https://developer.aircall.io/api-references/#tag")
    (source
    (http/get :url "/tags")
    (extract-path "tag")
    (setup-test
    (upon-receiving :code 200 (pass))))

    (fields
    id :id
    name
    color
    description
    )

)

 
