import random

MAX_SIZE = 60

f = open("test.txt","w+")

for i in range(8000):
    s = f"{i}="
    for i in range(10):
        s += str(float(random.randint(-999,999)) / 10)
    while len(s) < MAX_SIZE:
        s += "#"

    f.write(s+"\n")

