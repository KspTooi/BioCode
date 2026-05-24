package com.ksptool.bio.commons.dataprocess.converter;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ImageData;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.ksptool.bio.biz.core.common.model.ExtendedFileAttachJson;
import com.ksptool.bio.biz.core.service.AttachService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class AttachImagesConverter implements Converter<List<ExtendedFileAttachJson>> {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    @Setter
    private static AttachService attachService;

    @Override
    public Class<List<ExtendedFileAttachJson>> supportJavaTypeKey() {
        return (Class) List.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public List<ExtendedFileAttachJson> convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        return null;
    }

    @Override
    public WriteCellData<?> convertToExcelData(List<ExtendedFileAttachJson> value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        WriteCellData<Void> cellData = new WriteCellData<>();
        cellData.setType(CellDataTypeEnum.EMPTY);

        if (CollectionUtils.isEmpty(value)) {
            return cellData;
        }

        List<ImageData> imageDataList = new ArrayList<>();
        StringBuilder textBuilder = new StringBuilder();

        for (ExtendedFileAttachJson file : value) {
            if (file == null || file.getP() == null) {
                continue;
            }

            String name = file.getN() != null ? file.getN() : "附件";
            String path = file.getP();
            String ext = getExtension(name);

            if (isImageFile(ext)) {
                try {
                    byte[] bytes = readLocalFile(path);
                    if (bytes != null && bytes.length > 0) {
                        ImageData imageData = new ImageData();
                        imageData.setImage(bytes);
                        imageData.setImageType(getImageType(name));
                        imageDataList.add(imageData);
                    } else {
                        textBuilder.append(name).append(":").append(path).append("\n");
                    }
                } catch (Exception e) {
                    textBuilder.append(name).append(":").append(path).append("\n");
                }
            } else {
                textBuilder.append(name).append(":").append(path).append("\n");
            }
        }

        if (!imageDataList.isEmpty()) {
            cellData.setImageDataList(imageDataList);
        }

        if (textBuilder.length() > 0) {
            cellData.setType(CellDataTypeEnum.STRING);
            cellData.setStringValue(textBuilder.toString().trim());
        }

        return cellData;
    }

    private byte[] readLocalFile(String filePath) throws IOException {
        try {
            if (attachService == null) {
                log.warn("AttachService 未注入");
                return null;
            }
            Path absolutePath = attachService.getAttachLocalPath(Paths.get(filePath));
            if (absolutePath != null && Files.exists(absolutePath)) {
                return Files.readAllBytes(absolutePath);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private ImageData.ImageType getImageType(String fileName) {
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return ImageData.ImageType.PICTURE_TYPE_PNG;
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            return ImageData.ImageType.PICTURE_TYPE_JPEG;
        } else if (lowerName.endsWith(".bmp")) {
            return ImageData.ImageType.PICTURE_TYPE_DIB;
        } else if (lowerName.endsWith(".gif") || lowerName.endsWith(".webp")) {
            return ImageData.ImageType.PICTURE_TYPE_PNG;
        }
        return ImageData.ImageType.PICTURE_TYPE_PNG;
    }

    private boolean isImageFile(String ext) {
        if (ext == null) {
            return false;
        }
        return IMAGE_EXTENSIONS.contains(ext.toLowerCase());
    }

    private String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return null;
    }
}
