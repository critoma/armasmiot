# function definition
def sum(a, b):
   sum = a + b
   return sum

# call function
mysum = sum(5, 4)
print("Sum is ", mysum)

def interch(x, y):
   aux = x
   x = y
   y = aux
   return (x,y)

def interch2(x, y):
   x, y = y, x
   return (x, y)

v1 = 23.5
v2 = 17.1

v3 = 25
v4 = 10

print ("v1 = ", v1, ", v2 = ", v2)
(v1,v2) = interch(v1, v2)
print ("v1 = ", v1, ", v2 = ", v2)

print ("v3 = ", v3, ", v4 = ", v4)
(v3, v4) = interch2(v3, v4)
print ("v3 = ", v3, ", v4 = ", v4)
