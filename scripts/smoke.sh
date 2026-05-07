#!/usr/bin/env bash
set -euo pipefail

curl -fsS -c /tmp/jvm-session-cookies.txt http://127.0.0.1:8080/session/create
echo
curl -fsS -b /tmp/jvm-session-cookies.txt http://127.0.0.1:8081/session
echo

curl -fsS -c /tmp/native-session-cookies.txt http://127.0.0.1:8081/session/create
echo
curl -fsS -b /tmp/native-session-cookies.txt http://127.0.0.1:8080/session
echo
