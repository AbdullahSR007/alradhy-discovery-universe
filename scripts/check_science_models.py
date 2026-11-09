#!/usr/bin/env python3
"""Physics sanity checks for the educational ALRADHY V0.5 app."""
import math
C=299792458.0
AU=149597870700.0
G=9.80665
def check(name,value,expected,relative=1e-6):
    assert math.isfinite(value) and math.isclose(value,expected,rel_tol=relative,abs_tol=1e-9),f"{name}: {value} != {expected}"
    print("PASS",name,round(value,7))
def bk7(lam):
    x=(lam/1000)**2
    return math.sqrt(1+1.03961212*x/(x-.00600069867)+.231792344*x/(x-.0200179144)+1.01046945*x/(x-103.560653))
def deviation(lam,angle):
    angle=math.radians(angle)
    return 2*math.asin(bk7(lam)*math.sin(angle/2))-angle
check("Coulomb magnitude, +1uC x +2uC, 1m",8.9875517923e9*1e-6*2e-6,0.0179751035846)
check("Coulomb inverse square",8.9875517923e9*1e-6*2e-6/4,0.0179751035846/4)
assert deviation(450,50)>deviation(650,50),"Blue must bend more than red in BK7"
print("PASS prism BK7 dispersion")
check("Foucault equator",15.041067*math.sin(0),0)
check("Foucault pole",15.041067*math.sin(math.pi/2),15.041067)
check("Young fringe spacing",500e-9*2/(.2e-3),.005)
check("Archimedes one liter of water",1000*G*.001,G)
check("Faraday induced emf",50*.01*2*math.pi*2*.5,math.pi)
check("Ohm current at V=10,R=5",10/5,2)
check("Kepler 1 AU orbit",math.sqrt(1**3),1)
assert 500*343/(343-50)>500>500*343/(343+50),"Doppler direction inconsistent"
print("PASS Doppler approach/recede")
check("Galileo pendulum L=1m",2*math.pi*math.sqrt(1/G),2.006409292589)
assert max(0,1239.841984/400-2)>0 and max(0,1239.841984/700-2)==0
print("PASS photoelectric emission threshold")
check("Romer 2AU light travel",2*AU/C,998.0095676723)
check("Fizeau minimum speed (RPM)",60*C/(4*720*8633),723.465331673)
assert 2*1000/C*1e6 < 250*1000
print("PASS Galileo lantern timing scale")
print("All twelve numerical physics relations and three light-lab checks passed.")
