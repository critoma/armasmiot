#!/usr/bin/python

print "A. while loop statement sample"

count = 0
while (count < 9):
   print 'The count is:', count
   count = count + 1

print "1. Good bye! - while"

var = 1
while var != '0' :  # This constructs an infinite loop
   var = raw_input("Enter a number  :")
   print "You entered: ", var

print "2. Good bye! - while"

count = 0
while count < 5:
   print count, " is  less than 5"
   count = count + 1
else:
   print count, " is not less than 5"

print "3. Good bye! - while"



print "B. for loop statement sample"

for letter in 'Python':     # First Example
   print 'Current Letter :', letter

fruits = ['banana', 'apple',  'mango']
for fruit in fruits:        # Second Example
   print 'Current fruit :', fruit

print "4. Good bye! - for"


fruits = ['banana', 'apple',  'mango']
for index in range(len(fruits)):
   print 'Current fruit :', fruits[index]


print "5. Good bye! - for"


fruits = ['banana', 'apple',  'mango']
for index in range(len(fruits)):
   print 'Current fruit :', fruits[index]


print "6. Good bye! - for"

for num in range(10,20):     #to iterate between 10 to 20
   for i in range(2,num):    #to iterate on the factors of the number
      if num%i == 0:         #to determine the first factor
         j=num/i             #to calculate the second factor
         print '%d equals %d * %d' % (num,i,j)
         # break #to move to the next number, the #first FOR
      else:                  # else part of the loop
         print num, 'is a prime number'
         break #to move to the next number, the #first FOR

print "7. Good bye! - for"


print "C. nested loops"

i = 2
while(i < 20):
   j = 2
   while(j <= (i/j)):
      if not(i%j): break
      j = j + 1
   if (j > i/j) : print i, " is prime"
   i = i + 1

print "8. Good bye! - nested loops"
