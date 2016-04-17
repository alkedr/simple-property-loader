#!/bin/bash

set -xe

# Расшифровываем и распаковываем архив с секретными файлами. Он содержит:
#   - gpg-ключи, которыми maven-gpg-plugin будет подписывать артефакты (travis/public.key и travis/private.key)
#   - settings.xml с паролями от аккаунта sonatype, который используется для деплоя
# Travis не даёт ничего расшифровывать сборкам на основе пулл-реквестов, здесь дыры вроде нет.
# (https://docs.travis-ci.com/user/encryption-keys)
openssl aes-256-cbc -K $encrypted_5d1cc3ab8481_key -iv $encrypted_5d1cc3ab8481_iv -in travis-secrets.tar.enc -out travis-secrets.tar -d
tar xvf travis-secrets.tar

# импортируем gpg-ключи для подписывания артефактов
gpg --import travis/public.key
gpg --allow-secret-key-import --import travis/private.key

# деплоим, используя settings.xml с паролями от аккаунта sonatype
mvn deploy --settings travis/settings.xml -P deploy-profile
