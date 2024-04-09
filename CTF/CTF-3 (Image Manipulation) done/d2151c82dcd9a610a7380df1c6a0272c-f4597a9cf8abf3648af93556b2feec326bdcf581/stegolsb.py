#!/usr/bin/python3
# -*- coding: UTF-8 -*-
from PIL import Image
from tinyscript import *


__author__    = "Alexandre D'Hondt"
__version__   = "1.2"
__copyright__ = ("A. D'Hondt", 2020)
__license__   = "gpl-3.0"
__examples__  = [
    "-v extract -b 0 test.png",
    "extract test.png --cols 128 --rows 1 --column-step 2",
    "-w secret.txt bruteforce test.png",
]
__doc__       = """
*StegoLSB* allows to apply steganography based on LSB (Least Significant Bit) in order to retrieve hidden data from an image.
"""


BANNER_FONT       = "standard"
BANNER_STYLE      = {'fgcolor': "lolcat"}
HOTKEYS           = {'d': lambda: show_data(), 'h': lambda: show_data(first=1), 't': lambda: show_data(last=1)}
SCRIPTNAME_FORMAT = "none"


def show_data(first=0, last=0, width=16):
    global p
    if not hasattr(p, "data"):
        return
    d = p.data
    n_lines, PRINT = len(d) // width, re.sub(r"\s", "", string.printable)
    for i in range(0, len(d), width):
        if first > 0 and i // width >= first:
            break
        elif last > 0 and i // width <= n_lines - last:
            continue
        h = ts.str2hex(d[i:i+width])
        h = " ".join(h[j:j+4] for j in range(0, len(h), 4))
        b = "".join(c if c in PRINT else "." for c in d[i:i+width])
        print("%0.8x:  %s  %s" % (i, h.ljust(39), b))


class LSB(object):
    def __init__(self, image, secret=None):
        self.__image = image
        self.__secret = secret
        self.__write = True
        self.__obj = Image.open(image)
        logger.debug("Image size: {}x{}".format(*self.__obj.size))
    
    def bruteforce(self, bits=False, channels=False, nchars=16, maxstep=10):
        self.__write = False
        for ch in (ts.bruteforce(3, "RGB", repeat=False) if channels else ["RGB"]):
            for bi in (ts.bruteforce(8, range(8), repeat=False) if bits else [(0, )]):
                for y_s in range(1, maxstep + 1):
                    for x_s in range(1, maxstep + 1):
                        self.extract(bi, ch, colstep=x_s, rowstep=y_s)
                        for s in ts.strings(self.data, nchars):
                            logger.info(s)
                            if self.__secret:
                                self.write(s)
    
    def extract(self, bits=(0, ), channels="RGB", cols=None, rows=None, coloffset=0, rowoffset=0, colstep=1, rowstep=1):
        logger.debug("Bits {}, channels {}, column step {}, row step {}".format(list(bits), channels, colstep, rowstep))
        i = self.__obj
        cols = cols or i.size[0]
        rows = rows or i.size[1]
        self.data = ""
        for y in range(rowoffset, rows, max(1, rowstep)):
            data = ""
            for x in range(coloffset, cols, max(1, colstep)):
                pixel = {k: v for k, v in zip("RGB", i.getpixel((x, y)))}
                for c in channels.upper():
                    B = ts.int2bin(pixel[c])[::-1]
                    for b in bits:
                        data += B[b]
            d = ts.bin2str(data)
            self.data += d
            if self.__write:
                self.write(d)
        return self
    
    def hide(self, data):
        bin_data = ts.str2bin(data)
        #TODO: implement hiding data
        bin_len = ts.int2bin(len(bin_data))
        return self
    
    def write(self, content):
        fn = self.__secret
        if fn is None:
            fn = os.path.basename(self.__image)
            fn, _ = os.path.splitext(fn)
            fn = "{}-secret.txt".format(fn)
        with open(fn, 'ab') as f:
            f.write(b(content))
        return self


if __name__ == "__main__":
    parser.add_argument("-w", "--write", help="write data to a file")
    subparsers = parser.add_subparsers(help="commands", dest="command")
    extract = subparsers.add_parser('extract', help="manually extract hidden data")
    bruteforce = subparsers.add_parser('bruteforce', help="bruteforce parameters for extracting hidden data")
    extract.add_argument("image", type=ts.file_exists, help="image path")
    extract.add_argument("-b", "--bits", type=ts.pos_ints, default="0", help="bits to be considered, starting from LSB")
    extract.add_argument("-c", "--channels", default="RGB", help="channels to be considered")
    extract.add_argument("--columns", dest="cols", type=ts.pos_int, help="number of image columns to be considered")
    extract.add_argument("--column-offset", dest="coloffset", type=ts.pos_int,  default=0,
                        help="column offset for searching for data")
    extract.add_argument("--column-step", dest="colstep", type=ts.pos_int, default=1,
                         help="step number for iterating columns")
    extract.add_argument("--rows", type=ts.pos_int, help="number of image rows to be considered")
    extract.add_argument("--row-offset", dest="rowoffset", type=ts.pos_int, default=0,
                         help="row offset for searching for data")
    extract.add_argument("--row-step", dest="rowstep", type=ts.pos_int, default=1,
                         help="step number for iterating rows")
    bruteforce.add_argument("image", type=ts.file_exists, help="image path")
    bruteforce.add_argument("-b", "--bits", action="store_true", help="bruteforce the bits positions",
                            note="if false, only the LSB is considered")
    bruteforce.add_argument("-c", "--channels", action="store_true", help="bruteforce the color channels",
                            note="if false, RGB are considered")
    bruteforce.add_argument("-n", "--nchars", type=ts.pos_int, default=16, help="minimal length for readable strings")
    bruteforce.add_argument("-s", "--max-step", type=ts.pos_int, default=10, help="maximum bit step to be considered",
                            note="e.g. 3 will lookup every 3 bits in the LSB-collected data")
    initialize(noargs_action="demo")
    p = LSB(args.image, args.write)
    if args.command == "bruteforce":
        p.bruteforce(args.bits, args.channels, args.nchars, args.max_step)
    elif args.command == "extract":
        p.extract(args.bits, args.channels, args.cols, args.rows, args.coloffset, args.rowoffset, args.colstep,
                  args.rowstep)
        logger.info("Hidden data:\n" + p.data)