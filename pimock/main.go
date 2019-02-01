package main

import "net"
import "log"
import "math/rand"
import "time"
import "fmt"
import "strconv"
import "os"

func main() {
	tcpAddr, err := net.ResolveTCPAddr("tcp4", "127.0.0.1:8002")
	if err != nil {
		log.Fatal(err)
	}

	c, err := net.DialTCP("tcp", nil, tcpAddr)
	if err != nil {
		log.Fatal(err)
	}

	rand.Seed(time.Now().UnixNano())

	uts, err := strconv.Atoi(os.Args[1])
	if err != nil {
		fmt.Println("UTS pl0x")
		return
	}

	tm := time.Unix(int64(uts), 0)

	for {
		log.Println("Start")

		_, err = c.Write([]byte("START\n" + tm.Format("2006-01-02,15:04:05\n")))
		if err != nil {
			log.Fatal(err)
		}

		for i := 1000; i < 9000; i++ {
			_, err = c.Write([]byte(strconv.Itoa(i) + "," + fmt.Sprintf("%.02f", float32(rand.Intn(1000))/10-20) + "," + fmt.Sprintf("%.02f", float32(rand.Intn(255))/10) + "\n"))
			if err != nil {
				log.Fatal(err)
			}
		}

		_, err = c.Write([]byte("END\n"))
		if err != nil {
			log.Fatal(err)
		}

		tm = tm.Add(time.Second)
	}
}
