ip=`hostname -I|xargs`
api=$(</home/pi/apiKey)
for (( i = 0; i < 12; i++ )); do
	curl -X POST -H "Content-Type: application/json" --data "{\"apiKey\": \"$api\", \"ip\": \"$ip\"}" https://university-of-tehran.ml/api/register-repsbarrie-ip
	sleep 5
done
