import cv2

# Read the images
foo = cv2.imread("./first.png")
bar = cv2.imread("./second.png")

result = foo + bar
cv2.imwrite("./output.png", result)
