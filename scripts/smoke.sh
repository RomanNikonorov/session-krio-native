#!/usr/bin/env bash
set -euo pipefail

cookie_jar="${TMPDIR:-/tmp}/jvm-session-cookies.txt"

extract_session_id() {
  sed -E 's/.*"sessionId":"([^"]+)".*/\1/'
}

assert_same_session() {
  local expected="$1"
  local actual="$2"
  local step="$3"

  if [[ "$actual" != "$expected" ]]; then
    echo "Expected ${step} to use sessionId ${expected}, got ${actual}" >&2
    exit 1
  fi
}

echo "JVM creates session"
jvm_create_response="$(curl -fsS -c "$cookie_jar" http://127.0.0.1:8080/session/create)"
echo "$jvm_create_response"
jvm_create_session_id="$(echo "$jvm_create_response" | extract_session_id)"

echo "JVM reads created session"
jvm_read_response="$(curl -fsS -b "$cookie_jar" http://127.0.0.1:8080/session)"
echo "$jvm_read_response"
jvm_read_session_id="$(echo "$jvm_read_response" | extract_session_id)"
assert_same_session "$jvm_create_session_id" "$jvm_read_session_id" "JVM read"

echo "Native reads JVM session"
native_read_response="$(curl -fsS -b "$cookie_jar" http://127.0.0.1:8081/session)"
echo "$native_read_response"
native_read_session_id="$(echo "$native_read_response" | extract_session_id)"
assert_same_session "$jvm_create_session_id" "$native_read_session_id" "native read"

echo "OK: JVM and native app read sessionId ${jvm_create_session_id}"
