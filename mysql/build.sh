export REPO=reg.azan.kz/test-mysql:1.0.0
docker build -t ${REPO} .
docker push ${REPO}
