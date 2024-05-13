## StegoLSB

This [Tinyscript](https://github.com/dhondta/python-tinyscript)-based tool allows to apply steganography based on LSB (Least  Significant Bit) in order to retrieve hidden data from an image.

```session
$ pip install tinyscript
$ tsm install stegolsb
```

![](https://raw.githubusercontent.com/dhondta/python-tinyscript/master/docs/examples-rl/stegolsb.png)

This tool is especially useful in the use cases hereafter.

### Extract hidden data from an image using LSB stegano

```session
$ stegolsb -v extract test.png --column-step 2 --rows 1 --columns 128
12:34:56 [DEBUG] Image size: 225x225
12:34:56 [DEBUG] Bits [0], channels RGB, column step 2, row step 1
12:34:56 [INFO] Hidden data:
[...]
```


### Bruteforce LSB stegano parameters to recover hidden data from an image

This will display readable strings recovered using bruteforced paramters.

```session
$ stegolsb bruteforce test.png
12:34:56 [INFO] [...]
```
