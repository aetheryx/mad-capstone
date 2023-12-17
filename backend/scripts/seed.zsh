main_token=""

for account (user{1..9} 'main'); do
  main_token=$(curl "http://127.0.0.1:3000/auth/signup" \
    -X POST \
    -H "Content-Type: application/json" \
    -d "{ \
      \"username\": \"$account\", \
      \"password\": \"hunter2\", \
      \"avatar\": \"profile_pictures%252Fasdf.png%3Falt%3Dmedia%26token%3D3d5df673-2fa6-413d-8f8a-6c424d2e4c0b\" \
    }" -s | jq -r '.token')
; done

for id in {1..9}; do
  curl "http://127.0.0.1:3000/conversations" \
    -X POST \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $main_token" \
    -d "{ \
      \"other_user\": $id
    }"
; done

echo "$main_token"