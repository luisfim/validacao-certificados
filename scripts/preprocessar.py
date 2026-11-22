"""Recebe uma imagem e grava uma cópia em preto e branco para o OCR."""
import sys
import cv2

imagem = cv2.imread(sys.argv[1], cv2.IMREAD_GRAYSCALE)
if imagem is None:
    raise ValueError("Não foi possível abrir a imagem")
# Otsu escolhe o limiar de separação entre fundo e letras.
_, tratada = cv2.threshold(imagem, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
if not cv2.imwrite(sys.argv[2], tratada):
    raise ValueError("Não foi possível salvar a imagem")
