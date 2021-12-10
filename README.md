# Barcode Generator

This is a program that transforms some number into a PNG image of a barcode.
Currently, the program only supports UPC-A and EAN-13, but there are plans for UPC-E and EAN-8.

The current implementation uses a command-line interface, but the program is organized so that a GUI can be added.
Since this is just a random project and there are many online barcode generators that already exist, this may not happen.

## References

I used Wikipedia and some official documentation to find specifications for PNG files and barcodes. Wikipedia was sufficient for the barcodes, but I used W3C for a some parts of the PNG specification.

- GS1: [EAN/UPC barcodes](https://www.gs1.org/standards/barcodes/ean-upc)
- W3C: [Portable Network Graphics (PNG) Specification (Second Edition)](https://www.w3.org/TR/PNG/)
- Wikipedia: [International Article Number](https://en.wikipedia.org/wiki/International_Article_Number)
- Wikipedia: [Portable Network Graphics](https://en.wikipedia.org/wiki/Portable_Network_Graphics)
- Wikipedia: [Universal Product Code](https://en.wikipedia.org/wiki/Universal_Product_Code)