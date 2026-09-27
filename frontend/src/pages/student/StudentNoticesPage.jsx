import React, { useState, useEffect } from 'react';
import { noticeService } from '../../services/noticeService';
import PageHeader from '../../components/common/PageHeader';
import StatusBadge from '../../components/common/Badge';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import EmptyState from '../../components/common/EmptyState';
import SearchBar from '../../components/common/SearchBar';
import { FiCalendar, FiUser } from 'react-icons/fi';

const CATEGORIES = ['ALL', 'ACADEMIC', 'EXAM', 'EVENT', 'INTERNSHIP', 'PLACEMENT', 'IMPORTANT', 'GENERAL'];

export const StudentNoticesPage = () => {
  const [notices, setNotices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');

  const fetchNotices = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await noticeService.getMyNotices();
      setNotices(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load notices.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotices();
  }, []);

  const filteredNotices = notices.filter((n) => {
    const matchCat = selectedCategory === 'ALL' || n.category === selectedCategory;
    const matchSearch =
      !searchTerm ||
      n.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
      n.content.toLowerCase().includes(searchTerm.toLowerCase());
    return matchCat && matchSearch;
  });

  return (
    <div className="space-y-6">
      <PageHeader
        title="Notice Board"
        subtitle="Department announcements, examination notices, and internship postings"
      />

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
        <div className="flex flex-wrap gap-1.5">
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => setSelectedCategory(cat)}
              className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-colors ${
                selectedCategory === cat
                  ? 'bg-primary-700 text-white shadow-xs'
                  : 'bg-white text-slate-600 border border-slate-200 hover:bg-slate-50'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
        <div className="w-full sm:w-64">
          <SearchBar
            value={searchTerm}
            onChange={setSearchTerm}
            placeholder="Search announcements..."
          />
        </div>
      </div>

      {loading && <LoadingSpinner message="Fetching department notices..." />}
      {error && <ErrorState message={error} onRetry={fetchNotices} />}

      {!loading && !error && filteredNotices.length === 0 && (
        <EmptyState title="No notices matching the criteria" />
      )}

      {!loading && !error && filteredNotices.length > 0 && (
        <div className="space-y-4">
          {filteredNotices.map((n) => (
            <div
              key={n.noticeId}
              className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm hover:border-slate-300 transition-colors"
            >
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-slate-100">
                <div className="flex items-center gap-2 flex-wrap">
                  <h2 className="text-base font-bold text-slate-900">{n.title}</h2>
                  <StatusBadge status={n.category} />
                  <StatusBadge status={n.priority} />
                </div>
                <div className="flex items-center gap-3 text-xs text-slate-400">
                  <span className="flex items-center gap-1">
                    <FiCalendar className="w-3.5 h-3.5" />
                    Published: {n.publishedAt || n.publishDate}
                  </span>
                  {n.expiryDate && (
                    <span className="hidden sm:inline">Expires: {n.expiryDate}</span>
                  )}
                </div>
              </div>

              <p className="mt-3 text-sm text-slate-600 leading-relaxed whitespace-pre-line">
                {n.content}
              </p>

              <div className="mt-4 pt-3 border-t border-slate-100 flex items-center justify-between text-xs text-slate-400">
                <span className="flex items-center gap-1.5">
                  <FiUser className="w-3.5 h-3.5" />
                  Target: {n.targetProgramCode || n.targetProgram || 'All Programs'}
                </span>
                {n.authorName && <span>Posted by: {n.authorName}</span>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default StudentNoticesPage;
