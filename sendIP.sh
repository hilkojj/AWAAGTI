ip=`hostname -I|xargs`
curl -X POST -H "Content-Type: application/json" --data "{\"apiKey\": \"hallo ik ben chris en ik hou niet van windows en proprietary shit\", \"ip\": \"$ip\"}" https://university-of-tehran.ml/api/register-repsbarrie-ip
