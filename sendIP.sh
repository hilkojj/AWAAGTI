ip=`hostname -I|xargs`
for (( i = 0; i < 12; i++ )); do
	curl -X POST -H "Content-Type: application/json" --data "{\"apiKey\": \"hallo ik ben chris en ik hou niet van windows en proprietary shit\", \"ip\": \"$ip\"}" https://university-of-tehran.ml/api/register-repsbarrie-ip
	sleep 5
done
