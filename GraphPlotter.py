
import sys
import matplotlib.pyplot as plt
import numpy as np
import csv

data = np.genfromtxt(sys.argv[1],skiprows=1,delimiter=',')

x=data[:,1]
y=data[:,0]

plt.plot(x,y,'*',linewidth=1)
plt.plot(x,y,linewidth=1)
plt.title('X vs Y')
plt.xlabel('change this shot yo!')
plt.ylabel(r'change this shit too!')
plt.savefig('plottest1.png',dpi=100)
plt.clf()
