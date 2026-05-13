const PROFILE_IMAGE_SIZE = 256;
const PROFILE_IMAGE_QUALITY = 0.9;

export interface ProfileImageCropPosition {
  x: number;
  y: number;
}

export const DEFAULT_PROFILE_IMAGE_CROP: ProfileImageCropPosition = {
  x: 50,
  y: 50,
};

function clampPercent(value: number): number {
  if (Number.isNaN(value)) return 50;
  return Math.min(100, Math.max(0, value));
}

export function cropProfileImageToSquare(
  file: File,
  position: ProfileImageCropPosition = DEFAULT_PROFILE_IMAGE_CROP
): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onload = () => {
      const image = new Image();

      image.onload = () => {
        const sourceSize = Math.min(image.naturalWidth, image.naturalHeight);
        const maxSourceX = Math.max(0, image.naturalWidth - sourceSize);
        const maxSourceY = Math.max(0, image.naturalHeight - sourceSize);
        const sourceX = maxSourceX * (clampPercent(position.x) / 100);
        const sourceY = maxSourceY * (clampPercent(position.y) / 100);

        const canvas = document.createElement('canvas');
        canvas.width = PROFILE_IMAGE_SIZE;
        canvas.height = PROFILE_IMAGE_SIZE;

        const context = canvas.getContext('2d');
        if (!context) {
          reject(new Error('Não foi possível processar a imagem.'));
          return;
        }

        context.fillStyle = '#ffffff';
        context.fillRect(0, 0, PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE);
        context.drawImage(
          image,
          sourceX,
          sourceY,
          sourceSize,
          sourceSize,
          0,
          0,
          PROFILE_IMAGE_SIZE,
          PROFILE_IMAGE_SIZE
        );

        resolve(canvas.toDataURL('image/jpeg', PROFILE_IMAGE_QUALITY));
      };

      image.onerror = () => reject(new Error('Não foi possível carregar a imagem.'));
      image.src = reader.result as string;
    };

    reader.onerror = () => reject(new Error('Não foi possível ler o arquivo.'));
    reader.readAsDataURL(file);
  });
}
