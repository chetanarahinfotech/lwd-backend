package com.lwd.jobportal.dto.companydto;
import java.util.List; 
import lombok.AllArgsConstructor; 
import lombok.Data; 
@Data 
@AllArgsConstructor 
public class PagedCompanyResponse { 
	private List<CompanyResponse> content; 
	private int pageNumber; 
	private int pageSize; 
	private long totalElements; 
	private int totalPages; 
	private boolean last; 
}