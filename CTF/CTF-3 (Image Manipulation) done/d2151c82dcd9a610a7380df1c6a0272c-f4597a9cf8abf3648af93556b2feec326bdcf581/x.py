import cv2

# Read the images
foo = cv2.imread("./first.png")
bar = cv2.imread("./second.png")

# Perform bitwise XOR operation
key = cv2.bitwise_xor(foo, bar)

# Set non-white pixels to black
key_gray = cv2.cvtColor(key, cv2.COLOR_BGR2GRAY)
_, thresholded = cv2.threshold(key_gray, 240, 255, cv2.THRESH_BINARY_INV)

# Apply the thresholded mask to make non-white pixels black
result = cv2.bitwise_and(key, key, mask=thresholded)

# Save the result
cv2.imwrite("./output.png", result)
