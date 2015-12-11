import sys
import matplotlib.pyplot as plt
import numpy as np
import csv

data = np.genfromtxt(sys.argv[1],skiprows=1,delimiter=',')
data2 = np.genfromtxt(sys.argv[2],skiprows=1,delimiter=',')
data3 = np.genfromtxt(sys.argv[3],skiprows=1,delimiter=',')


x=data[:,0]
y=data[:,1]

x2=data2[:,0]
y2=data2[:,1]

x3=data3[:,0]
y3=data3[:,1]



x = [float(x_val) for x_val in x]
y = [float(y_val) for y_val in y]

x2 = [float(x_val) for x_val in x2]
y2 = [float(y_val) for y_val in y2]

x3 = [float(x_val) for x_val in x3]
y3 = [float(y_val) for y_val in y3]


print y
print y2
print y3



i_s, = plt.plot(x,y,'+',label='Idle Sense')
e_b, = plt.plot(x2,y2,'+',label='Exponential Backoff')
o_q, = plt.plot(x3,y3,'+',label='1/Q')

plt.legend(loc='lower right',numpoints=1)

plt.xlabel('Time in Seconds')
plt.ylabel("Jain's Fairness Index")
plt.savefig('fig13.png',dpi=100)

