import sys
from operator import itemgetter

with open(sys.argv[1],"r") as f:
    data = f.readlines()

    data = [line.replace('\n','').split(",") for line in data]
    data = sorted(data, key=itemgetter(5),reverse=True)
    for line in data[:5]:
        print line