tuple = ( 'abcd', 786 , 2.23, 'john', 70.2  )
tinytuple = (123, 'john')

print (tuple)           # Prints complete tuple
print (tuple[0])        # Prints first element of the tuple
print (tuple[1:3])      # Prints elements starting from 2nd till 3rd 
print (tuple[2:])       # Prints elements starting from 3rd element
print (tinytuple * 2)   # Prints tuple two times
print (tuple + tinytuple) # Prints concatenated tuple

tuplex = ( 'abcd', 786 , 2.23, 'john', 70.2  )
listx = [ 'abcd', 786 , 2.23, 'john', 70.2  ]
# tuplex[2] = 1000    # Invalid syntax with tuple
listx[2] = 1000     # Valid syntax with list