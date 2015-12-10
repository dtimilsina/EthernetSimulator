import sys
import matplotlib.pyplot as plt
import numpy as np
import csv

data = np.genfromtxt(sys.argv[1],skiprows=1,delimiter=',')
data2 = np.genfromtxt(sys.argv[2],skiprows=1,delimiter=',')
data3 = np.genfromtxt(sys.argv[3],skiprows=1,delimiter=',')
#data4 = np.genfromtxt(sys.argv[4],skiprows=1,delimiter=',')


x=data[:,0]
y=data[:,1]

x2=data2[:,0]
y2=data2[:,1]

x3=data3[:,0]
y3=data3[:,1]

'''
x4=data4[:,0]
y4=data4[:,1]
'''

x = [int(x_val) for x_val in x]
y = [float(y_val) for y_val in y]

x2 = [int(x_val) for x_val in x2]
y2 = [float(y_val) for y_val in y2]

x3 = [int(x_val) for x_val in x3]
y3 = [float(y_val) for y_val in y3]

'''
x4 = [int(x_val) for x_val in x4]
y4 = [float(y_val) for y_val in y4]
'''

i_s, = plt.plot(x,y,'+',label='Idle Sense')
e_b, = plt.plot(x2,y2,'+',label='Exponential Backoff')
o_q, = plt.plot(x3,y3,'+',label='1/Q')
#plt.plot(x4,y4,'+',label='Ideal Idle Sense')

plt.legend(loc='lower right',numpoints=1)

plt.xlabel('Number of Iterations')
plt.ylabel("Jain's Fairness Index")
plt.savefig('fig13.png',dpi=100)