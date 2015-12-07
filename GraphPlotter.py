import sys
import matplotlib.pyplot as plt
import numpy as np
import csv

data = np.genfromtxt(sys.argv[1],skiprows=1,delimiter=',')

x=data[:,0]
y=data[:,1]
z=data[:,2]

x = [int(x_val) for x_val in x]
y = [int(y_val) for y_val in y]
z = [float(y_val) for y_val in z]


#plt.ylim(0,10)
#plt.xlim(0,10)
for bytesVal in y:
    hosts = [a for a,b,c in zip(x,y,z) if b == bytesVal]
    print hosts
    vals = [c for a,b,c in zip(x,y,z) if b == bytesVal]
    print vals
    plt.plot(hosts,vals,'+')
    plt.plot(hosts,vals,linewidth=1)
    print "____________"

'''
#Figure 3.3
plt.ylim(7,10)
plt.title('Total Bit Rate')
plt.xlabel('Number of Hosts')
plt.ylabel('Ethernet Utilization in MBits\Sec')
plt.savefig('fig3-3.png',dpi=100)

'''

#Figure 3.5
plt.title('Total Packet Rate')
plt.xlabel('Number of Hosts')
plt.ylabel('Ethernet Utilization in Packets\Sec')
plt.savefig('fig3-5.png',dpi=100)

'''

#Figure 3.7
plt.title('Average Tranmission Delay')
plt.xlabel('Number of Hosts')
plt.ylabel('Average Tranmission Delay in Milliseconds')
plt.savefig('fig3-7.png',dpi=100)
'''
plt.clf()

