package com.ksptool.bio.commons.dataprocess;

import com.alibaba.excel.metadata.data.ImageData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.handler.context.CellWriteHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.Units;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AttachImageWriteHandler implements CellWriteHandler {

    private static final int SINGLE_IMG_WIDTH_PX = 150;
    private static final int IMG_HEIGHT_PX = 120;
    private static final int IMG_GAP_PX = 4;

    private final Map<String, List<ImageData>> pendingImages = new HashMap<>();

    @Override
    public void afterCellDataConverted(CellWriteHandlerContext context) {
        WriteCellData<?> firstCellData = context.getFirstCellData();
        if (firstCellData == null) {
            return;
        }
        List<ImageData> imageDataList = firstCellData.getImageDataList();
        if (imageDataList == null || imageDataList.isEmpty()) {
            return;
        }
        String key = context.getRowIndex() + "_" + context.getColumnIndex();
        pendingImages.put(key, new ArrayList<>(imageDataList));
        firstCellData.setImageDataList(null);
    }

    @Override
    public void afterCellDispose(CellWriteHandlerContext context) {
        Cell cell = context.getCell();
        int colIdx = cell.getColumnIndex();
        int rowIdx = cell.getRowIndex();
        String key = rowIdx + "_" + colIdx;
        List<ImageData> imageDataList = pendingImages.remove(key);
        if (imageDataList == null || imageDataList.isEmpty()) {
            return;
        }

        Sheet sheet = context.getWriteSheetHolder().getSheet();
        Workbook workbook = sheet.getWorkbook();

        Drawing<?> drawing = sheet.getDrawingPatriarch();
        if (drawing == null) {
            drawing = sheet.createDrawingPatriarch();
        }

        CreationHelper helper = workbook.getCreationHelper();

        int imgCount = imageDataList.size();
        int singleWidthEmu = Units.pixelToEMU(SINGLE_IMG_WIDTH_PX);
        int heightEmu = Units.pixelToEMU(IMG_HEIGHT_PX);


        for (int i = 0; i < imgCount; i++) {
            ImageData imgData = imageDataList.get(i);
            byte[] pictureBytes = imgData.getImage();
            if (pictureBytes == null || pictureBytes.length == 0) {
                continue;
            }

            int pictureType = mapImageType(imgData.getImageType());
            int index = workbook.addPicture(pictureBytes, pictureType);

            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(colIdx);
            anchor.setRow1(rowIdx);
            anchor.setCol2(colIdx);
            anchor.setRow2(rowIdx);

            int dx1 = singleWidthEmu * i + Units.pixelToEMU(IMG_GAP_PX * i);
            anchor.setDx1(dx1);
            anchor.setDx2(dx1 + singleWidthEmu);
            anchor.setDy1(0);
            anchor.setDy2(heightEmu);
            log.info("图片的地址：{}，宽：{}-{}，高：{}-{}",i,anchor.getDx1(),anchor.getDx2(),anchor.getDy1(),anchor.getDy2());
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
            drawing.createPicture(anchor, index);
        }

        Row row = sheet.getRow(rowIdx);
        if (row != null) {
            short targetHeight = (short) (IMG_HEIGHT_PX * 20);
            if (row.getHeight() < targetHeight) {
                row.setHeight(targetHeight);
            }
        }

        int targetColWidth = SINGLE_IMG_WIDTH_PX * imgCount + IMG_GAP_PX * (imgCount - 1);
        int targetColWidthInChars = (int) ((targetColWidth / 8.43F) * 256);
        if (sheet.getColumnWidth(colIdx) < targetColWidthInChars) {
            sheet.setColumnWidth(colIdx, targetColWidthInChars);
        }
        log.info("图片的cell：宽：{}，高：{}",sheet.getColumnWidth(colIdx), sheet.getDefaultRowHeight());
    }

    private int mapImageType(ImageData.ImageType imageType) {
        if (imageType == null) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        return switch (imageType) {
            case PICTURE_TYPE_JPEG -> Workbook.PICTURE_TYPE_JPEG;
            default -> Workbook.PICTURE_TYPE_PNG;
        };
    }
}
