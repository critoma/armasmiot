class Student:
    noStudents = 0

    def __init__(self, name, id):
        self.name = name
        self.id = id
        Student.noStudents += 1
        return

    def displayCount():
        print ("Total number of students = %d" % Student.noStudents)
        return
	
    def displayStudent(self):
        print ("Student ", self.name, " has id = ", self.id)
        return

s1 = Student("Zara", 1111)
s2 = Student("Iulius", 2222);
s1.displayStudent()
s2.displayStudent()
Student.displayCount()
