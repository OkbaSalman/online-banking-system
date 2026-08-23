import React, { useCallback, useEffect, useState } from 'react';
import {
  CheckCircle2,
  Clock,
  Download,
  FileText,
  ShieldAlert,
  ShieldCheck,
  Upload,
} from 'lucide-react';
import AppLayout from '../components/layout/AppLayout';
import {
  Badge,
  Button,
  Card,
  DataRow,
  EmptyState,
  ErrorNotice,
  InfoNotice,
  SectionTitle,
  SelectField,
  Spinner,
  StatusBadge,
  TextAreaField,
  TextField,
} from '../components/ui';
import { useToast } from '../context/ToastContext';
import * as kycService from '../services/kycService';
import { documentTypeLabel, formatDateTime, kycStatusLabel } from '../lib/format';

const MAX_FILE_BYTES = 10 * 1024 * 1024;

function formatBytes(bytes) {
  if (!bytes) return '0 KB';
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function CompliancePage() {
  const toast = useToast();

  const [application, setApplication] = useState(null);
  const [documents, setDocuments] = useState([]);
  const [loading, setLoading] = useState(true);

  const [fullName, setFullName] = useState('');
  const [nationalId, setNationalId] = useState('');
  const [address, setAddress] = useState('');
  const [selectedDocIds, setSelectedDocIds] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);

  const [documentType, setDocumentType] = useState('ID_FRONT');
  const [uploadStatus, setUploadStatus] = useState(null);
  const [uploadError, setUploadError] = useState(null);
  const [uploading, setUploading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    const [applicationResult, documentsResult] = await Promise.allSettled([
      kycService.getMyKyc(),
      kycService.listMyDocuments(),
    ]);

    if (applicationResult.status === 'fulfilled' && applicationResult.value) {
      const app = applicationResult.value;
      setApplication(app);
      setFullName((value) => value || app.fullName || '');
      setNationalId((value) => value || app.nationalId || '');
      setAddress((value) => value || app.address || '');
    }
    if (documentsResult.status === 'fulfilled') {
      setDocuments(documentsResult.value);
      setSelectedDocIds(documentsResult.value.map((document) => document.id));
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const status = application?.status || 'KYC_STATUS_NOT_SUBMITTED';
  const isPending = status.includes('PENDING');
  const isApproved = status.includes('APPROVED');

  const handleUpload = async (event) => {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;

    setUploadError(null);
    if (file.size > MAX_FILE_BYTES) {
      setUploadError('That file is larger than 10 MB. Upload a smaller scan or photo.');
      return;
    }

    setUploading(true);
    try {
      await kycService.uploadDocument(file, documentType, setUploadStatus);
      toast.success('Document uploaded', `${file.name} is attached to your application.`);
      setUploadStatus(null);
      await load();
    } catch (err) {
      setUploadError(err.message);
      setUploadStatus(null);
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (documentId) => {
    try {
      const url = await kycService.getDocumentDownloadUrl(documentId);
      window.open(url, '_blank', 'noopener');
    } catch (err) {
      toast.error('Could not open the document', err.message);
    }
  };

  const toggleDocument = (documentId) => {
    setSelectedDocIds((current) =>
      current.includes(documentId)
        ? current.filter((id) => id !== documentId)
        : [...current, documentId],
    );
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitError(null);

    if (!fullName.trim() || !nationalId.trim() || !address.trim()) {
      return setSubmitError('Fill in your legal name, national id and address.');
    }
    if (selectedDocIds.length === 0) {
      return setSubmitError('Attach at least one identity document before submitting.');
    }

    setSubmitting(true);
    try {
      const result = await kycService.submitKyc({
        fullName: fullName.trim(),
        nationalId: nationalId.trim(),
        address: address.trim(),
        documentIds: selectedDocIds,
      });
      setApplication(result);
      toast.success('Application submitted', 'A reviewer will check your documents shortly.');
      await load();
    } catch (err) {
      setSubmitError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout
      eyebrow="Compliance"
      title="Identity verification"
      description="Confirm who you are so the bank can lift limits on transfers, cards and bill payments."
    >
      {loading ? (
        <Card>
          <Spinner label="Loading your application…" />
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-8 lg:grid-cols-12">
          <div className="space-y-6 lg:col-span-5">
            <Card className="p-6">
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-center gap-3">
                  <div
                    className={`flex h-11 w-11 items-center justify-center rounded-full ${
                      isApproved
                        ? 'bg-emerald-50 text-emerald-600'
                        : isPending
                          ? 'bg-amber-50 text-amber-600'
                          : 'bg-[#eff4ff] text-[#3525cd]'
                    }`}
                  >
                    {isApproved ? (
                      <CheckCircle2 size={20} />
                    ) : isPending ? (
                      <Clock size={20} />
                    ) : (
                      <ShieldAlert size={20} />
                    )}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-[#0b1c30]">Verification status</p>
                    <p className="text-[11px] text-[#777587]">{kycStatusLabel(status)}</p>
                  </div>
                </div>
                <StatusBadge status={status} />
              </div>

              {application?.rejectionReason && (
                <div className="mt-4">
                  <ErrorNotice>{application.rejectionReason}</ErrorNotice>
                </div>
              )}

              {application?.id && status !== 'KYC_STATUS_NOT_SUBMITTED' && (
                <div className="mt-4 divide-y divide-slate-100 rounded-2xl bg-slate-50 p-4">
                  <DataRow label="Submitted">{formatDateTime(application.createdAtEpochMs)}</DataRow>
                  <DataRow label="Last update">{formatDateTime(application.updatedAtEpochMs)}</DataRow>
                  {application.reviewerUserId && (
                    <DataRow label="Reviewed by">
                      <span className="break-all font-mono text-[11px]">{application.reviewerUserId}</span>
                    </DataRow>
                  )}
                </div>
              )}
            </Card>

            <Card className="p-6">
              <SectionTitle
                icon={Upload}
                title="Upload documents"
                description="Files go straight to encrypted object storage; only their hash and metadata pass through the bank."
              />

              <div className="space-y-4">
                <SelectField
                  label="Document type"
                  value={documentType}
                  onChange={(event) => setDocumentType(event.target.value)}
                  disabled={uploading}
                >
                  {kycService.DOCUMENT_TYPES.map((type) => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </SelectField>

                <div className="rounded-2xl border-2 border-dashed border-slate-200 bg-slate-50 p-6 text-center">
                  <input
                    type="file"
                    id="kyc_file"
                    className="hidden"
                    accept="image/*,application/pdf"
                    onChange={handleUpload}
                    disabled={uploading}
                  />
                  <label
                    htmlFor="kyc_file"
                    className={`flex flex-col items-center ${uploading ? 'cursor-wait opacity-60' : 'cursor-pointer'}`}
                  >
                    <Upload className="mb-2 text-slate-400" size={24} />
                    <span className="text-xs font-bold text-[#464555]">
                      {uploading ? 'Uploading…' : 'Choose a file'}
                    </span>
                    <span className="mt-1 text-[10px] text-slate-400">PDF or image, up to 10 MB</span>
                  </label>
                </div>

                {uploadStatus && (
                  <p className="text-center font-mono text-[11px] text-[#3525cd]">{uploadStatus}</p>
                )}
                <ErrorNotice>{uploadError}</ErrorNotice>
                {uploadError && (
                  <p className="text-[11px] leading-relaxed text-[#777587]">
                    The gateway already created the document slot — only the browser→MinIO PUT failed.
                    Open the MinIO host in your firewall, set <code className="font-mono text-[#3525cd]">MINIO_PUBLIC_ENDPOINT</code>{' '}
                    to that reachable URL, recreate <code className="font-mono">kyc-service</code>, and run{' '}
                    <code className="font-mono">ops/minio/configure-cors.sh</code>.
                  </p>
                )}
              </div>
            </Card>
          </div>

          <div className="space-y-6 lg:col-span-7">
            <Card className="p-6 sm:p-8">
              <SectionTitle
                icon={FileText}
                title={`Your documents (${documents.length})`}
                description="Tick the documents to include in your application."
              />

              {documents.length === 0 ? (
                <EmptyState
                  icon={FileText}
                  title="No documents uploaded"
                  description="Upload an ID document and a proof of address to get started."
                />
              ) : (
                <div className="space-y-2">
                  {documents.map((document) => {
                    const checked = selectedDocIds.includes(document.id);
                    return (
                      <div
                        key={document.id}
                        className={`flex items-center gap-3 rounded-xl border p-3 transition-colors ${
                          checked ? 'border-[#3525cd]/30 bg-[#eff4ff]' : 'border-slate-100 bg-white'
                        }`}
                      >
                        <input
                          type="checkbox"
                          checked={checked}
                          onChange={() => toggleDocument(document.id)}
                          disabled={isPending || isApproved}
                          className="h-4 w-4 shrink-0 cursor-pointer accent-[#3525cd]"
                        />
                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-2">
                            <p className="truncate text-xs font-bold text-[#0b1c30]">
                              {document.originalFilename}
                            </p>
                            <Badge tone="indigo">{documentTypeLabel(document.type)}</Badge>
                          </div>
                          <p className="mt-0.5 text-[10px] text-slate-400">
                            {formatBytes(document.sizeBytes)} · uploaded{' '}
                            {formatDateTime(document.uploadedAtEpochMs)}
                          </p>
                        </div>
                        <button
                          type="button"
                          onClick={() => handleDownload(document.id)}
                          className="shrink-0 cursor-pointer rounded-lg p-2 text-slate-400 transition-colors hover:bg-slate-100 hover:text-[#3525cd]"
                          aria-label="Open document"
                        >
                          <Download size={15} />
                        </button>
                      </div>
                    );
                  })}
                </div>
              )}
            </Card>

            <Card className="p-6 sm:p-8">
              <SectionTitle
                icon={ShieldCheck}
                title={isApproved ? 'Verified details' : 'Your details'}
                description={
                  isApproved
                    ? 'These details were approved by a reviewer.'
                    : 'They must match the documents you uploaded.'
                }
              />

              <form onSubmit={handleSubmit} className="space-y-4">
                <ErrorNotice>{submitError}</ErrorNotice>

                <TextField
                  label="Full legal name"
                  value={fullName}
                  onChange={(event) => setFullName(event.target.value)}
                  placeholder="As printed on your ID"
                  disabled={isPending || isApproved}
                  required
                />

                <TextField
                  label="National id or passport number"
                  value={nationalId}
                  onChange={(event) => setNationalId(event.target.value)}
                  placeholder="AB1234567"
                  disabled={isPending || isApproved}
                  required
                />

                <TextAreaField
                  label="Residential address"
                  value={address}
                  onChange={(event) => setAddress(event.target.value)}
                  placeholder="Street, city, postal code, country"
                  disabled={isPending || isApproved}
                  required
                />

                {isApproved ? (
                  <InfoNotice icon={CheckCircle2} title="You are verified.">
                    All limits tied to identity verification have been lifted on your accounts.
                  </InfoNotice>
                ) : isPending ? (
                  <InfoNotice icon={Clock} title="Under review.">
                    Your application is queued for a compliance officer. You will be notified by
                    email once a decision is made.
                  </InfoNotice>
                ) : (
                  <Button type="submit" size="lg" loading={submitting} icon={ShieldCheck}>
                    Submit for review
                  </Button>
                )}
              </form>
            </Card>
          </div>
        </div>
      )}
    </AppLayout>
  );
}
