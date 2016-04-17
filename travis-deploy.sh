#!/bin/bash

set -xe

openssl aes-256-cbc -K $encrypted_473dbd0066c2_key -iv $encrypted_473dbd0066c2_iv -in travis-secrets.tar.enc -out travis-secrets.tar -d
tar xvf travis-secrets.tar

gpg --import travis/public.key
gpg --allow-secret-key-import --import travis/private.key

mvn deploy --settings travis/settings.xml -P deploy-profile
