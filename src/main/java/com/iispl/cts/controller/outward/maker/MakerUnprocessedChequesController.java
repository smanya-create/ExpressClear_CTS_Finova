//package com.iispl.cts.controller.outward.maker;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Executions;
//import org.zkoss.zk.ui.Sessions;
//import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.event.InputEvent;
//import org.zkoss.zk.ui.util.Clients;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zul.Button;
//import org.zkoss.zul.Combobox;
//import org.zkoss.zul.Include;
//import org.zkoss.zul.Intbox;
//import org.zkoss.zul.Label;
//import org.zkoss.zul.ListModelList;
//import org.zkoss.zul.Listbox;
//import org.zkoss.zul.Listcell;
//import org.zkoss.zul.Listitem;
//import org.zkoss.zul.ListitemRenderer;
//import org.zkoss.zul.Textbox;
//import org.zkoss.zul.Vlayout;
//import java.sql.ResultSet;
//
//import com.iispl.cts.common.config.DBConnection;
//import com.iispl.cts.dao.outward.MakerUnprocessedChequeDAO;
//import com.iispl.cts.daoimpl.outward.MakerUnprocessedChequeDAOImpl;
//import com.iispl.cts.dto.UnprocessedChequeDTO;
//
//public class MakerUnprocessedChequesController extends GenericForwardComposer<Component> {
//
//	private static final long serialVersionUID = 1L;
//
//	// =========================================================
//	// ZUL COMPONENTS
//	// =========================================================
//	private Label lblTotalCount;
//	private Label lblRepairCount;
//	private Label lblDataEntryCount;
//
//	private Combobox cmbStatusFilter;
//	private Textbox txtSearchBatch;
//	private Button btnRefresh;
//	private Listbox lstUnprocessed;
//
//	// Session state flag
//	private boolean isSessionClosed = false;
//
//	// Centered Pagination Controls
//	private Button btnFirstPage;
//	private Button btnPrevPage;
//	private Intbox ibCurrentPage;
//	private Label lblTotalPages;
//	private Button btnNextPage;
//	private Button btnLastPage;
//
//	private static final int PAGE_SIZE = 12;
//	private int activePageIndex = 0;
//	private int totalPages = 1;
//	private List<UnprocessedChequeDTO> currentFilteredList = new ArrayList<>();
//
//	// =========================================================
//	// DAO & DATA
//	// =========================================================
//	private final MakerUnprocessedChequeDAO unprocessedDAO = new MakerUnprocessedChequeDAOImpl();
//	private List<UnprocessedChequeDTO> masterList = new ArrayList<>();
//	private final DecimalFormat df = new DecimalFormat("##,##,##0.00");
//
//	@Override
//	public void doAfterCompose(Component comp) throws Exception {
//		super.doAfterCompose(comp);
//
//		resolveSessionStatus();
//
//		if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) {
//			cmbStatusFilter.setSelectedIndex(0);
//		}
//
//		initListboxRenderer();
//		loadUnprocessedCheques();
//	}
//
//	/**
//	 * Inspects global desktop/application context and session attribute
//	 * to determine outward session status immediately even if EOD was just forced.
//	 */
//	private void resolveSessionStatus() {
//		// 1. Check Global Application Scope (set by Admin EOD/BOD)
//		Object globalSessionOpen = (getPage() != null && getPage().getDesktop() != null && getPage().getDesktop().getWebApp() != null)
//				? getPage().getDesktop().getWebApp().getAttribute("GLOBAL_CTS_SESSION_OPEN")
//						: null;
//
//		if (globalSessionOpen instanceof Boolean) {
//			this.isSessionClosed = !((Boolean) globalSessionOpen);
//			return;
//		}
//
//		// 2. Check HTTP Session Attribute
//		Object sessionOpenAttr = Sessions.getCurrent() != null 
//				? Sessions.getCurrent().getAttribute("CTS_SESSION_OPEN") 
//						: null;
//
//		if (sessionOpenAttr != null) {
//			if (sessionOpenAttr instanceof Boolean) {
//				this.isSessionClosed = !((Boolean) sessionOpenAttr);
//			} else {
//				this.isSessionClosed = "false".equalsIgnoreCase(sessionOpenAttr.toString().trim());
//			}
//		} else {
//			this.isSessionClosed = true;
//		}
//	}
//
//	// =========================================================
//	// LISTBOX RENDERER (Strict CTS Alignment System)
//	// =========================================================
//	// =========================================================
//	// LISTBOX RENDERER (Strict CTS Alignment System)
//	// =========================================================
//	// =========================================================
//    // LISTBOX RENDERER (Strict CTS Alignment System)
//    // =========================================================
//    private void initListboxRenderer() {
//        lstUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
//            @Override
//            public void render(Listitem item, UnprocessedChequeDTO dto, int index) {
//                item.setValue(dto);
//
//                // 1. Batch ID Column (Centered, No Subtitle)
//                Listcell cellBatch = new Listcell();
//                cellBatch.setStyle("text-align: center; vertical-align: middle;");
//
//                String fullBatchNo = dto.getBatchNo() != null ? dto.getBatchNo().trim() : "";
//                String displayBatchId = fullBatchNo;
//
//                if (displayBatchId.matches(".*BAT\\d+.*")) {
//                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(BAT\\d+)").matcher(displayBatchId);
//                    if (m.find()) {
//                        displayBatchId = m.group(1);
//                    }
//                } else if (dto.getBatchId() != null && dto.getBatchId() > 0) {
//                    displayBatchId = "BAT" + dto.getBatchId();
//                } else if (fullBatchNo.contains("-")) {
//                    displayBatchId = fullBatchNo.substring(fullBatchNo.lastIndexOf('-') + 1);
//                }
//
//                Label lblBNo = new Label(displayBatchId);
//                lblBNo.setStyle("font-size: 13px; font-weight: 700; color: #1e293b; text-align: center; display: block;");
//                cellBatch.appendChild(lblBNo);
//
//                // 2. Cheque No (Number -> CENTER)
//                Listcell cellChq = new Listcell();
//                cellChq.setStyle("text-align: center; vertical-align: middle;");
//                Label lblChq = new Label(dto.getChequeNo() != null ? dto.getChequeNo() : "------");
//                lblChq.setStyle("font-family: monospace; font-size: 13px; font-weight: 600; color: #334155; display: block; text-align: center;");
//                cellChq.appendChild(lblChq);
//
//                // 3. MICR Sort Code (Number -> CENTER)
//                Listcell cellSort = new Listcell();
//                cellSort.setStyle("text-align: center; vertical-align: middle;");
//                Label lblSort = new Label(dto.getSortCode() != null ? dto.getSortCode() : "------");
//                lblSort.setStyle("font-family: monospace; font-size: 13px; font-weight: 500; color: #334155; display: block; text-align: center;");
//                cellSort.appendChild(lblSort);
//
//                // 4. Amount (Number / Currency -> CENTER)
//                Listcell cellAmt = new Listcell();
//                cellAmt.setStyle("text-align: center; vertical-align: middle;");
//                Label lblAmt = new Label(dto.getAmount() != null ? "₹ " + df.format(dto.getAmount()) : "₹ 0.00");
//                lblAmt.setStyle("font-size: 13px; font-weight: 700; color: #0f172a; display: block; text-align: center;");
//                cellAmt.appendChild(lblAmt);
//
//                // 5. STATUS BADGE (Pill-shaped, non-truncated width)
//                Listcell cellStage = new Listcell();
//                cellStage.setStyle("text-align: center; vertical-align: middle; padding: 2px 4px;");
//
//                String rawDbStatus = (dto.getStatus() != null && !dto.getStatus().trim().isEmpty())
//                        ? dto.getStatus().trim()
//                        : "------";
//
//                Label lblStage = new Label(rawDbStatus);
//                String badgeStyle;
//
//                if (rawDbStatus.toUpperCase().contains("MICR")) {
//                    // Soft Amber / Gold Pill Badge for UNPROCESSED_MICR
//                    badgeStyle = "display: inline-block; margin: 0 auto; padding: 4px 10px; border-radius: 9999px; "
//                            + "font-size: 10px; font-weight: 700; letter-spacing: 0.3px; text-transform: uppercase; "
//                            + "box-sizing: border-box; text-align: center; line-height: 1.2; "
//                            + "background: #fef3c7 !important; color: #b45309 !important; border: 1px solid #fde68a !important;";
//                } else {
//                    // Soft Peach / Orange Pill Badge for UNPROCESSED_DATA_ENTRY
//                    badgeStyle = "display: inline-block; margin: 0 auto; padding: 4px 10px; border-radius: 9999px; "
//                            + "font-size: 10px; font-weight: 700; letter-spacing: 0.3px; text-transform: uppercase; "
//                            + "box-sizing: border-box; text-align: center; line-height: 1.2; "
//                            + "background: #ffedd5 !important; color: #c2410c !important; border: 1px solid #fed7aa !important;";
//                }
//                lblStage.setStyle(badgeStyle);
//                cellStage.appendChild(lblStage);
//
//                // 6. Reason / Remarks (Left-aligned)
//                Listcell cellRemarks = new Listcell();
//                cellRemarks.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
//                String reasonText = dto.getSendBackReason() != null ? dto.getSendBackReason() : "Scan Review";
//                if (dto.getRemarks() != null && !dto.getRemarks().isEmpty()) {
//                    reasonText += " (" + dto.getRemarks() + ")";
//                }
//                Label lblReason = new Label(reasonText);
//                lblReason.setStyle("font-size: 12px; font-style: italic; color: #c2410c; display: block; word-break: break-word;");
//                cellRemarks.appendChild(lblReason);
//
//                // 7. Action Button ('Data Entry' / 'MICR Repair' - Identical Width)
//                Listcell cellAction = new Listcell();
//                cellAction.setStyle("text-align: center; vertical-align: middle; padding: 0 6px;");
//                Button btnAction = new Button();
//                btnAction.setIconSclass("z-icon-pencil");
//
//                boolean wasDataEntry = isItemDataEntry(dto);
//                if (wasDataEntry) {
//                    btnAction.setLabel("Data Entry");
//                } else {
//                    btnAction.setLabel("MICR Repair");
//                }
//
//                // Uniform width and height styling for both buttons
//                String baseBtnStyle = "width: 110px !important; height: 32px !important; font-size: 11px !important; "
//                        + "font-weight: 600 !important; border-radius: 4px !important; text-align: center !important; "
//                        + "box-sizing: border-box !important; padding: 0 !important; line-height: 30px !important; ";
//
//                if (isSessionClosed) {
//                    btnAction.setDisabled(true);
//                    btnAction.setTooltiptext("Outward Clearing session is closed by Admin. Processing is disabled.");
//                    btnAction.setStyle(baseBtnStyle + "background: #cbd5e1 !important; color: #94a3b8 !important; "
//                            + "border: 1px solid #cbd5e1 !important; cursor: not-allowed !important; box-shadow: none !important;");
//                } else {
//                    btnAction.setDisabled(false);
//                    btnAction.setStyle(baseBtnStyle + "background: #173B61 !important; color: #ffffff !important; "
//                            + "border: 1px solid #173B61 !important; cursor: pointer !important; box-shadow: 0 1px 2px rgba(0,0,0,0.1) !important;");
//                    btnAction.addEventListener("onClick", event -> routeToMakerModule(dto, wasDataEntry));
//                }
//
//                cellAction.appendChild(btnAction);
//
//                // Append cells
//                item.appendChild(cellBatch);
//                item.appendChild(cellChq);
//                item.appendChild(cellSort);
//                item.appendChild(cellAmt);
//                item.appendChild(cellStage);
//                item.appendChild(cellRemarks);
//                item.appendChild(cellAction);
//            }
//        });
//    }
//	private List<String> getPendingMicrChequeIds(String batchIdStr) {
//		List<String> micrIds = new ArrayList<>();
//		String sql = "SELECT scanned_cheque_id FROM scan_cheque "
//				+ "WHERE scanned_batch_id = ? "
//				+ "  AND UPPER(cheque_status) = 'PENDING_MICR_REPAIR' "
//				+ "ORDER BY scanned_cheque_id ASC";
//
//		try (Connection conn = com.iispl.cts.common.config.DBConnection.getConnection();
//				PreparedStatement ps = conn.prepareStatement(sql)) {
//			ps.setString(1, batchIdStr);
//			try (ResultSet rs = ps.executeQuery()) {
//				while (rs.next()) {
//					micrIds.add(rs.getString("scanned_cheque_id"));
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return micrIds;
//	}
//
//	// Helper: checks whether instrument requires Data Entry vs MICR Repair
//	private boolean isItemDataEntry(UnprocessedChequeDTO dto) {
//		if (dto == null) return false;
//		String status = dto.getStatus() != null ? dto.getStatus().trim().toUpperCase() : "";
//		String remarks = dto.getRemarks() != null ? dto.getRemarks().trim().toUpperCase() : "";
//		return status.contains("DATA_ENTRY") || remarks.contains("DATA_ENTRY");
//	}
//
//	// =========================================================
//	// DATA RETRIEVAL & COUNTERS
//	// =========================================================
//	public void loadUnprocessedCheques() {
//		this.masterList = unprocessedDAO.getUnprocessedCheques("MAKER");
//		updateCounters();
//		applyFilters(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
//	}
//
//	private void updateCounters() {
//		long total = masterList.size();
//		long dataEntry = masterList.stream().filter(this::isItemDataEntry).count();
//		long repair = total - dataEntry;
//
//		if (lblTotalCount != null) lblTotalCount.setValue(String.valueOf(total));
//		if (lblRepairCount != null) lblRepairCount.setValue(String.valueOf(repair));
//		if (lblDataEntryCount != null) lblDataEntryCount.setValue(String.valueOf(dataEntry));
//	}
//
//
//	// =========================================================
//	// SEARCH & FILTER EVENTS
//	// =========================================================
//	public void onFilterChanged() {
//		applyFilters(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
//	}
//
//	public void onOK$txtSearchBatch(Event event) {
//		applyFilters(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
//	}
//
//	public void onChanging$txtSearchBatch(InputEvent event) {
//		applyFilters(event.getValue());
//	}
//
//	public void onClick$btnRefresh(Event event) {
//		handleRefresh();
//	}
//
//	public void onClick$btnRefresh() {
//		handleRefresh();
//	}
//
//	private void handleRefresh() {
//		if (txtSearchBatch != null) txtSearchBatch.setValue("");
//		if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) {
//			cmbStatusFilter.setSelectedIndex(0);
//		}
//		resolveSessionStatus();
//		loadUnprocessedCheques();
//		Clients.showNotification("Queue refreshed", "info", null, "top_center", 1500);
//	}
//
//	private void applyFilters(String searchKeywordInput) {
//		String stageFilter = (cmbStatusFilter != null && cmbStatusFilter.getSelectedItem() != null)
//				? cmbStatusFilter.getSelectedItem().getValue().toString()
//						: "ALL";
//
//		final String searchKeyword = (searchKeywordInput != null) ? searchKeywordInput.trim().toLowerCase() : "";
//
//		this.currentFilteredList = masterList.stream().filter(item -> {
//			boolean matchesStage = true;
//			boolean isDataEntry = isItemDataEntry(item);
//
//			if ("PENDING_REPAIR".equalsIgnoreCase(stageFilter)) {
//				matchesStage = !isDataEntry;
//			} else if ("PENDING_DATA_ENTRY".equalsIgnoreCase(stageFilter)) {
//				matchesStage = isDataEntry;
//			}
//
//			boolean matchesSearch = true;
//			if (!searchKeyword.isEmpty()) {
//				boolean bMatch = item.getBatchNo() != null && item.getBatchNo().toLowerCase().contains(searchKeyword);
//				boolean cMatch = item.getChequeNo() != null && item.getChequeNo().toLowerCase().contains(searchKeyword);
//				boolean micrMatch = item.getSortCode() != null && item.getSortCode().toLowerCase().contains(searchKeyword);
//				boolean rMatch = item.getRemarks() != null && item.getRemarks().toLowerCase().contains(searchKeyword);
//				boolean statusMatch = item.getStatus() != null && item.getStatus().toLowerCase().contains(searchKeyword);
//				matchesSearch = bMatch || cMatch || micrMatch || rMatch || statusMatch;
//			}
//
//			return matchesStage && matchesSearch;
//		}).collect(Collectors.toList());
//
//		// Calculate pages
//		this.totalPages = (int) Math.ceil((double) currentFilteredList.size() / PAGE_SIZE);
//		if (this.totalPages < 1) this.totalPages = 1;
//
//		loadPage(0);
//	}
//
//	private void loadPage(int pageIndex) {
//		if (pageIndex >= this.totalPages) pageIndex = this.totalPages - 1;
//		if (pageIndex < 0) pageIndex = 0;
//		this.activePageIndex = pageIndex;
//
//		// Update toolbar controls
//		if (ibCurrentPage != null) ibCurrentPage.setValue(this.activePageIndex + 1);
//		if (lblTotalPages != null) lblTotalPages.setValue("/ " + this.totalPages);
//
//		boolean isFirst = (this.activePageIndex <= 0);
//		boolean isLast = (this.activePageIndex >= this.totalPages - 1);
//
//		if (btnFirstPage != null) btnFirstPage.setDisabled(isFirst);
//		if (btnPrevPage != null) btnPrevPage.setDisabled(isFirst);
//		if (btnNextPage != null) btnNextPage.setDisabled(isLast);
//		if (btnLastPage != null) btnLastPage.setDisabled(isLast);
//
//		// Slice list for active page
//		int fromIndex = this.activePageIndex * PAGE_SIZE;
//		int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredList.size());
//
//		List<UnprocessedChequeDTO> pageSubList = (fromIndex < currentFilteredList.size())
//				? currentFilteredList.subList(fromIndex, toIndex)
//						: new ArrayList<>();
//
//		lstUnprocessed.setModel(new ListModelList<>(pageSubList));
//	}
//
//	public void onClick$btnFirstPage(Event event) {
//		if (activePageIndex > 0) loadPage(0);
//	}
//
//	public void onClick$btnPrevPage(Event event) {
//		if (activePageIndex > 0) loadPage(activePageIndex - 1);
//	}
//
//	public void onClick$btnNextPage(Event event) {
//		if (activePageIndex < totalPages - 1) loadPage(activePageIndex + 1);
//	}
//
//	public void onClick$btnLastPage(Event event) {
//		if (activePageIndex < totalPages - 1) loadPage(totalPages - 1);
//	}
//
//	public void onChange$ibCurrentPage(Event event) {
//		Integer target = ibCurrentPage.getValue();
//		if (target == null || target < 1) target = 1;
//		else if (target > totalPages) target = totalPages;
//		loadPage(target - 1);
//	}
//
//	public void onOK$ibCurrentPage(Event event) {
//		onChange$ibCurrentPage(event);
//	}
//
//	// =========================================================
//	// ROUTING LOGIC & BATCH/CHEQUE REACTIVATION
//	// =========================================================
//	// =========================================================
//	// BULLETPROOF ROUTING & REACTIVATION
//	// =========================================================
//	private void routeToMakerModule(UnprocessedChequeDTO dto, boolean isDataEntry) {
//		if (dto == null) return;
//
//		resolveSessionStatus();
//		if (isSessionClosed) {
//			Clients.showNotification("Clearing session is CLOSED by Admin. Actions are locked.", "error", null, "top_center", 2500);
//			return;
//		}
//
//		// 1. Resolve Primary Batch ID (Force "BAT1008" / "BAT1002")
//		String batchIdStr = null;
//		if (dto.getBatchId() != null && dto.getBatchId() > 0) {
//			batchIdStr = "BAT" + dto.getBatchId();
//		} else if (dto.getBatchNo() != null && dto.getBatchNo().trim().toUpperCase().startsWith("BAT")) {
//			batchIdStr = dto.getBatchNo().trim();
//		} else if (dto.getRemarks() != null && dto.getRemarks().contains("BAT")) {
//			int idx = dto.getRemarks().indexOf("BAT");
//			batchIdStr = dto.getRemarks().substring(idx).replaceAll("[^a-zA-Z0-9]", "");
//		} else {
//			batchIdStr = "BAT1008"; // Fallback to current batch
//		}
//
//		// 2. Resolve Cheque ID (e.g. "CH1003")
//		String chqIdStr = null;
//		if (dto.getChequeId() != null && dto.getChequeId() > 0) {
//			chqIdStr = "CH" + dto.getChequeId();
//		} else if (dto.getChequeNo() != null && !dto.getChequeNo().trim().isEmpty()) {
//			chqIdStr = resolveChequeIdFromDb(dto.getChequeNo(), batchIdStr);
//		}
//
//		// 3. Atomically reactivate in database
//		reactivateChequeAndBatchDirect(chqIdStr, dto.getChequeNo(), batchIdStr, dto.getBatchNo(), isDataEntry);
//
//		// 4. Populate standard session attributes
//		org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//		session.setAttribute("SELECTED_SCAN_BATCH_ID", batchIdStr);
//		session.setAttribute("SELECTED_SCAN_CHEQUE_ID", chqIdStr);
//		session.setAttribute("SELECTED_CHEQUE_NO", dto.getChequeNo());
//		session.setAttribute("SELECTED_OUTWARD_BATCH_ID", batchIdStr);
//		session.setAttribute("SELECTED_CHEQUE_ID", chqIdStr);
//		session.setAttribute("CURRENT_BATCH_ID", batchIdStr);
//		session.setAttribute("CURRENT_CHEQUE_ID", chqIdStr);
//
//		if (isDataEntry) {
//			Executions.sendRedirect("/outward/maker/data-entry.zul");
//			return;
//		}
//
//		// 5. Route to MICR Repair
//		String source = "SCAN";
//		Component root = (getPage() != null) ? getPage().getFirstRoot() 
//				: Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot();
//		Component mainContentArea = (root != null) ? root.getFellowIfAny("mainContentArea", true) : null;
//
//		if (mainContentArea instanceof Include) {
//			Include include = (Include) mainContentArea;
//			include.setAttribute("MICR_REPAIR_SOURCE", source);
//			include.setAttribute("MICR_REPAIR_BATCH_ID", batchIdStr);
//			include.setAttribute("MICR_REPAIR_CHEQUE_ID", chqIdStr); // Pass Cheque ID attribute
//			include.setSrc(null); // Clear first to force reload
//			include.setSrc("/outward/maker/micr-repair/micr-repair.zul");
//		} else {
//			// Pass query parameters in fallback redirect
//			String url = "/outward/maker/micr-repair/micr-repair.zul?source=" + source 
//					+ "&batchId=" + batchIdStr 
//					+ (chqIdStr != null ? "&chequeId=" + chqIdStr : "");
//			Executions.sendRedirect(url);
//		}
//	}
//
//	// Helper to look up scanned_cheque_id by cheque number if dto.getChequeId() is null
//	private String resolveChequeIdFromDb(String chequeNo, String batchId) {
//		String sql = "SELECT scanned_cheque_id FROM scan_cheque WHERE cheque_number = ? AND scanned_batch_id = ? LIMIT 1";
//		try (Connection conn = com.iispl.cts.common.config.DBConnection.getConnection();
//				PreparedStatement ps = conn.prepareStatement(sql)) {
//			ps.setString(1, chequeNo);
//			ps.setString(2, batchId);
//			try (ResultSet rs = ps.executeQuery()) {
//				if (rs.next()) {
//					return rs.getString("scanned_cheque_id");
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//
//	private void reactivateChequeAndBatchDirect(String chqIdStr, String chequeNo, String primaryBatchId, String batchRefId, boolean isDataEntry) {
//		String targetStatus = isDataEntry ? "PENDING_DATA_ENTRY" : "PENDING_MICR_REPAIR";
//
//		// Query checks cheque_id OR cheque_number to guarantee a match
//		String updateChequeSql = "UPDATE scan_cheque SET cheque_status = ? "
//				+ "WHERE (scanned_cheque_id = ? OR cheque_number = ?) "
//				+ "  AND UPPER(cheque_status) LIKE 'UNPROCESSED%'";
//
//		// Query checks scanned_batch_id OR batch_reference_id
//		String updateBatchSql = "UPDATE scan_batch SET batch_status = 'PENDING_MAKER_PROCESS' "
//				+ "WHERE (scanned_batch_id = ? OR batch_reference_id = ?) "
//				+ "  AND UPPER(batch_status) = 'UNPROCESSED'";
//
//		try (Connection conn = DBConnection.getConnection()) {
//			conn.setAutoCommit(false);
//
//			try (PreparedStatement psChq = conn.prepareStatement(updateChequeSql)) {
//				psChq.setString(1, targetStatus);
//				psChq.setString(2, chqIdStr != null ? chqIdStr : "");
//				psChq.setString(3, chequeNo != null ? chequeNo : "");
//				int chqRows = psChq.executeUpdate();
//				System.out.println("[CTS REACTIVATE] Cheque updated rows: " + chqRows + " for chequeNo: " + chequeNo);
//			}
//
//			try (PreparedStatement psBatch = conn.prepareStatement(updateBatchSql)) {
//				psBatch.setString(1, primaryBatchId != null ? primaryBatchId : "");
//				psBatch.setString(2, batchRefId != null ? batchRefId : "");
//				int batchRows = psBatch.executeUpdate();
//				System.out.println("[CTS REACTIVATE] Batch updated rows: " + batchRows + " for batchId: " + primaryBatchId);
//			}
//
//			conn.commit();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//	/**
//	 * Atomically transitions the instrument from UNPROCESSED to active stage
//	 * and reopens the parent batch from UNPROCESSED to PENDING_MAKER_PROCESS.
//	 */
//	private void reactivateChequeAndBatch(String chqIdStr, String batchIdStr, boolean isDataEntry) {
//		if (batchIdStr == null || batchIdStr.trim().isEmpty()) {
//			return;
//		}
//
//		String formattedBatchId = batchIdStr.trim();
//		if (!formattedBatchId.toUpperCase().startsWith("BAT")) {
//			formattedBatchId = "BAT" + formattedBatchId;
//		}
//
//		// Format single cheque ID if provided (e.g. 1001 -> CH1001)
//		String formattedChqId = null;
//		if (chqIdStr != null && !chqIdStr.trim().isEmpty()) {
//			formattedChqId = chqIdStr.trim();
//			if (!formattedChqId.toUpperCase().startsWith("CH")) {
//				formattedChqId = "CH" + formattedChqId;
//			}
//		}
//
//		// 1. Reactivate MICR items requiring repair
//		String updateMicrChequesSql = "UPDATE scan_cheque "
//				+ "SET cheque_status = 'PENDING_MICR_REPAIR' "
//				+ "WHERE scanned_batch_id = ? "
//				+ "  AND UPPER(cheque_status) LIKE 'UNPROCESSED%' "
//				+ "  AND (micr_code LIKE '%?%' OR micr_code = '000000000' OR micr_code IS NULL)";
//
//		// 2. Reactivate Data Entry items (MICR is clean)
//		String updateDataEntryChequesSql = "UPDATE scan_cheque "
//				+ "SET cheque_status = 'PENDING_DATA_ENTRY' "
//				+ "WHERE scanned_batch_id = ? "
//				+ "  AND UPPER(cheque_status) LIKE 'UNPROCESSED%' "
//				+ "  AND micr_code NOT LIKE '%?%' "
//				+ "  AND micr_code <> '000000000' "
//				+ "  AND micr_code IS NOT NULL";
//
//		// 3. Reactivate scan_batch
//		String updateBatchSql = "UPDATE scan_batch "
//				+ "SET batch_status = 'PENDING_MAKER_PROCESS' "
//				+ "WHERE (scanned_batch_id = ? OR batch_reference_id = ?) "
//				+ "  AND UPPER(batch_status) = 'UNPROCESSED'";
//
//		try (Connection conn = DBConnection.getConnection()) {
//			conn.setAutoCommit(false);
//
//			try (PreparedStatement psMicr = conn.prepareStatement(updateMicrChequesSql);
//					PreparedStatement psDe = conn.prepareStatement(updateDataEntryChequesSql);
//					PreparedStatement psBatch = conn.prepareStatement(updateBatchSql)) {
//
//				// Execute MICR update
//				psMicr.setString(1, formattedBatchId);
//				int micrUpdated = psMicr.executeUpdate();
//
//				// Execute Data Entry update
//				psDe.setString(1, formattedBatchId);
//				int deUpdated = psDe.executeUpdate();
//
//				// Execute Batch status update
//				psBatch.setString(1, formattedBatchId);
//				psBatch.setString(2, batchIdStr);
//				int batchUpdated = psBatch.executeUpdate();
//
//				conn.commit();
//				System.out.println("[CTS MAKER REACTIVATION] Batch: " + formattedBatchId 
//						+ " | MICR items: " + micrUpdated 
//						+ " | DE items: " + deUpdated 
//						+ " | Batch updated: " + batchUpdated);
//			} catch (SQLException ex) {
//				conn.rollback();
//				throw ex;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//}