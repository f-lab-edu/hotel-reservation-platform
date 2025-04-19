package com.reservation.fileupload.imageupload.controller.dto.request;

import com.reservation.common.uploadedimage.domain.ImageDomain;

public record UploadImageRequest(
	ImageDomain domain
) {
}
