import matplotlib.pyplot as plt

def opt_idle(val):
	n,peopt = val
	pi = (1-peopt)**n
	return pi / (1-pi)

def cw_idle(val):
	n,peopt = val
	return (1.0 / peopt)

Tc = 187.26
vals = [ (1, 1.0), (2, .623),(3 , .437),(4 , .335),(5 , .271),(6 , .227),
		(7 , .196),(8 , .173),(9 , .154),(10, .139),(11, .127),(12, .116),
		(13, .1074),(14, .0998),(15, .0933),(16, .0875),(17, .0824),(18, .0779),
		(19, .0737),(20, .07021),(21, .06691),(22, .0639),(23, .06114),(24, .05862)]

print "PE OPT { " + ", ".join([str(x[1]) for x in vals]) + " }"
print "CW OPT { " + ", ".join([str(x[1]/2) for x in vals]) + " }"

print "Tc= {0}".format(Tc)
print "{ " + ", ".join([str(opt_idle(val)) for val in vals]) + "}"

Tc = Tc / 2
vals = [ (1, 1.0000),(2, 0.5390),(3, 0.3647),(4, 0.2753),(5, 0.2211),(6, 0.1846),
		 (7, 0.1585),(8, 0.1389),(9, 0.1235),(10, 0.1113),(11, 0.1012),(12, 0.09284),
		 (13, 0.08573),(14, 0.07963),(15, 0.07435),(16, 0.06972),(17, 0.06563),(18, 0.06200),
		 (19, 0.05874),(20, 0.05582),(21, 0.05316),(22, 0.05076),(23, 0.04856 ),(24, 0.04654)]

print "PE HALF OPT { " + ", ".join([str(x[1]) for x in vals]) + " }"
print "CW OPT { " + ", ".join([str(x[1]/2) for x in vals]) + " }"

print "Tc= {0}".format(Tc)
print "{ " + ", ".join([str(opt_idle(val)) for val in vals]) + "}"
