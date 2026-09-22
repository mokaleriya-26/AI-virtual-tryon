# VirtualFit AI

A complete production-ready AI Virtual Try-On web application.

## AI Model Architecture
This project integrates with a free Hugging Face ZeroGPU space to run the **IDM-VTON** model (`yisol/IDM-VTON`).

### ⚠️ NON-COMMERCIAL USE ONLY
The IDM-VTON model is currently listed as **Non-Commercial Use Only** by its creators. Therefore:
1. This integration is strictly intended for academic, research, or demonstration purposes.
2. It is not commercially licensed.

### AI Provider & Inference
- **AI Provider**: Hugging Face ZeroGPU
- **Model**: IDM-VTON
- **Inference Mechanism**: Free Hugging Face ZeroGPU Space (`yisol/IDM-VTON`)

*Note: Free ZeroGPU usage is subject to Hugging Face's current daily quotas and queue availability. During periods of high traffic, inference may take longer or temporarily fail until quota is restored.*

## Development

1. **Run Backend:**
   ```bash
   cd backend
   npm run dev
   ```

2. **Run Frontend:**
   ```bash
   cd frontend
   npm run dev
   ```
