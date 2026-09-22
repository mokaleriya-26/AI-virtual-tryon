# VirtualFit AI Image Directory Architecture

This folder contains the local image assets for the VirtualFit AI frontend.

## Product Catalog Photos
Add product photos inside the appropriate category folder. The filenames **must** exactly match the IDs defined in `frontend/src/data/products.js`. 

Use lowercase filenames with hyphens (e.g. `black-oversized-tshirt.jpg`). 

### Category Folders:

- **T-SHIRTS:**
  `frontend/public/images/products/tshirts/`
  *Expected files:*
  - `black-oversized-tshirt.jpg`
  - `white-basic-tshirt.jpg`
  - `lavender-tshirt.jpg`

- **SHIRTS:**
  `frontend/public/images/products/shirts/`
  *Expected files:*
  - `classic-white-shirt.jpg`
  - `navy-casual-shirt.jpg`
  - `cream-linen-shirt.jpg`

- **DRESSES:**
  `frontend/public/images/products/dresses/`
  *Expected files:*
  - `rose-satin-dress.jpg`
  - `black-bodycon-dress.jpg`
  - `floral-summer-dress.jpg`
  - `burgundy-midi-dress.jpg`

- **JACKETS:**
  `frontend/public/images/products/jackets/`
  *Expected files:*
  - `vintage-denim-jacket.jpg`
  - `olive-oversized-jacket.jpg`

- **JEANS:**
  `frontend/public/images/products/jeans/`
  *Expected files:*
  - `blue-straight-jeans.jpg`
  - `black-wide-leg-pants.jpg`

- **TOPS:**
  `frontend/public/images/products/tops/`
  *Expected files:*
  - `lavender-crop-top.jpg`
  - `white-ribbed-top.jpg`
  - `chocolate-brown-top.jpg`

- **SWEATERS:**
  `frontend/public/images/products/sweaters/`
  *Expected files:*
  - `beige-knit-sweater.jpg`

- **SKIRTS:**
  `frontend/public/images/products/skirts/`
  *(Currently no seeded products, but available for expansion)*

## Marketing & Hero Images
For Home page marketing images and abstract backgrounds:
`frontend/public/images/hero/`
*Examples: `hero-fashion-1.jpg`, `stylefit-model.jpg`*

## StyleFit UI Examples
For optional UI examples in the Try-On flow:
`frontend/public/images/stylefit/`
*Examples: `stylefit-example-1.jpg`*
